# meta-entropy-tss

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

Instructions for building are in [entropyxyz/yocto-build](https://github.com/entropyxyz/yocto-build)
