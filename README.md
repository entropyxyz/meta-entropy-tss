# meta-entropy

Yocto layer with recipe for entropy-tss

This repo is currently **work-in-progress**

Currently this copies in a pre-built binary rather than compiling it within Yocto.

The included binary is not the 'real' entropy-tss binary, but a simple axum-based http server which on getting an HTTP request generates a TDX quote and responds with some quote data:

Eg: 
```
$ curl 34.147.25.103:3001
mrtd: 5d56080eb9ef8ce0bbaf6bdcdadeeb06e7c5b0a4d1ec16be868a85a953babe0c5e54d01c8e050a54fe1ca078372530d2
rtmr0: 4216e925f796f4e282cfa6e72d4c77a80560987afa29155a61fdc33adb80eab0d4112abd52387e5e25a60deefb8a5287
rtmr1: ef6e5150b2a490970ce987815d89e94ec8057de9e3586c61e14525870c75b798bb8e08a4bda6b9eaa4e7fd283a792f46
rtmr2: 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
rtmr3: 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
Signature: b32e06dac38d39bae549c171123c064f836539967d2c95170d96ccee64178d487071bd5c1b8c416d22d7918b6661e72b12f35f563554c42d1c1a8543bbd35841
mrseam: bfb360ac8e6233a1bca1433caf7382d95c165b4a77fb00bf1435e5a08f300cdfead5ee68461afd9b6c728dce7534602d
tee tcb svn 08010800000000000000000000000000
Generation: 6
```

It has an init script for the simple tiny-init system used by poky-tiny.

## To build an image for running on a TDX-enabled Google Cloud Platform instance:

### Setup build environment

- Install [repo](https://gerrit.googlesource.com/git-repo/+/HEAD/README.md)
- Follow instructions from the [flashbots/yocto-manifests v3 refactor PR](https://github.com/flashbots/yocto-manifests/pull/20):

```
mkdir entropy-tss-image-build && cd entropy-tss-image-build
repo init -u https://github.com/flashbots/yocto-manifests.git -b v3 -m tdx-rbuilder.xml
sed -i 's/main/v3/g' .repo/manifests/tdx-rbuilder.xml
repo sync
source setup
```

- Add this repository as a layer (TODO detailed instructions)

- `cd srcs/poky/meta-confidential-compute` and apply the following diff:

```
diff --git a/conf/distro/cvm.conf b/conf/distro/cvm.conf
index 68c7a12..37ad2e3 100644
--- a/conf/distro/cvm.conf
+++ b/conf/distro/cvm.conf
@@ -79,7 +79,7 @@ CVM_DISTRO_FEATURES_NATIVE = "acl debuginfod ext2 ipv4 ipv6 xattr nfs zeroconf p
 #DISTRO_FEATURES:class-native = "${CVM_DISTRO_FEATURES_NATIVE} tpm2"
 #DISTRO_FEATURES:class-nativesdk = "${CVM_DISTRO_FEATURES_NATIVE} tpm2"

-DISTRO_EXTRA_RDEPENDS = "date-sync ca-certificates"
+DISTRO_EXTRA_RDEPENDS = "entropy-tss date-sync ca-certificates"
 DISTRO_EXTRA_RDEPENDS:append = '${@bb.utils.contains_any("IMAGE_FEATURES", [ "debug-tweaks" ], " dropbear", "",d)}'

 # FIXME: Consider adding "modules" to MACHINE_FEATURES and using that in
diff --git a/recipes-kernel/linux/linux-yocto/gcp.cfg b/recipes-kernel/linux/linux-yocto/gcp.cfg
index 83c9421..2e0a3c2 100644
--- a/recipes-kernel/linux/linux-yocto/gcp.cfg
+++ b/recipes-kernel/linux/linux-yocto/gcp.cfg
@@ -24,3 +24,4 @@ CONFIG_NVME_FC=y
 CONFIG_NVME_TCP=y
 CONFIG_NVME_HOST_AUTH=y
 CONFIG_NVME_TARGET=y
+CONFIG_DMA_COHERENT_POOL=y
```

- TODO I think we should be able to modify `DISTRO_EXTRA_RDEPENDS` from `build/conf/local.conf` without needing to modify this repo.
- TODO The kernel config option edit can be made into a PR to `flashbots/meta-confidential-compute` once we figure out what other options are needed to fix warnings and panic.

### Build the image:

```
cd ../../..
DEBUG_TWEAKS_ENABLED=1 MACHINE=tdx-gcp SSH_PUBKEY=<some ssh public key> make build
```

Alternatively, there are also [instructions for building with docker](https://github.com/flashbots/yocto-manifests?tab=readme-ov-file#alternative-build-with-docker) which works great - but i think to use it we will need to fork `flashbot/yocto-manifests` and put our changes there.

- TODO im not sure if `MACHINE ?= "tdx-gcp"` also needs to be set in `./srcs/poky/build/conf/local.conf`

### Copy the built image to a GCP bucket:

```
gcloud storage buckets create gs://tss-cvm-images
gcloud storage cp srcs/poky/build/tmp/deploy/images/tdx-gcp/core-image-minimal-tdx-gcp.rootfs.wic.tar.gz gs://tss-cvm-images
```

### Create a GCP image from the image file:

```
gcloud compute images create core-image-minimal-tdx-gcp-3 --source-uri gs://cvm-images-flashbots/core-image-minimal-tdx-gcp.rootfs.wic.tar.gz --guest-os-features=UEFI_COMPATIBLE,VIRTIO_SCSI_MULTIQUEUE,GVNIC,TDX_CAPABLE
```

### Setup a GCP firewall rule to allow traffic to port 3001

```
$ gcloud compute firewall-rules create allow-port-3001 \
  --allow tcp:3001 \
  --target-tags entropy-tss \
  --description "Allow traffic on port 3001" \
  --direction INGRESS \
  --priority 1000 \
  --network default
```

### Start a GCP instance using the image:

```
gcloud compute instances create core-image-minimal-tdx-gcp-3 --network=default --confidential-compute-type=TDX --machine-type=c3-standard-4 --maintenance-policy=TERMINATE --image core-image-minimal-tdx-gcp-3 --zone=europe-west4-b --metadata serial-port-enable=TRUE --tags entropy-tss
```
