IMAGE_ROOTFS_SIZE = "11534336"

do_install:append() {
    install -d ${D}/persist
    echo "/dev/nvme0n1p2  /persist  ext4  defaults  0  0" >> ${D}/etc/fstab
}
