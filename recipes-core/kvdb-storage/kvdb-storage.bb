DESCRIPTION = "Additional Filesystem for KVDB"
LICENSE = "MIT"

# A pre-built ext-4 filesystem image (1MB)
# created like this:
# dd if=/dev/zero of=fs-image.img bs=1M count=1
# mkfs.ext4 fs-image.img
SRC_URI = "file://fs-image.img"

S = "${WORKDIR}"

do_install() {
    # Install the image into the final image's filesystem
    install -d ${D}${sysconfdir}/kvdb-fs
    install -m 0644 ${WORKDIR}/fs-image.img ${D}${sysconfdir}/kvdb-fs/
}

# Add the image to the final image
IMAGE_INSTALL:append = " kvdb-fs"
