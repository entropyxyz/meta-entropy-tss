# meta-entropy-tss

Yocto layer with recipe for building and running [entropy-tss](https://github.com/entropyxyz/entropy-core/tree/master/crates/threshold-signature-server) in a confidential virtual machine.

To check the measurement value, you can use the version endpoint:

Eg: 
```
$ curl 34.147.25.103:3001/version
```

It has an init script for the simple tiny-init system used by poky-tiny.

Instructions for building are in [entropyxyz/yocto-build](https://github.com/entropyxyz/yocto-build)
