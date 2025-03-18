DESCRIPTION = "Copy binary to the image"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"
BINARY = "entropy-tss"
SRC_URI = "file://${BINARY} file://init"
S = "${WORKDIR}"

INITSCRIPT_NAME = "${BINARY}"
INITSCRIPT_PARAMS = "defaults 99"

inherit update-rc.d

do_install() {
    install -d ${D}${bindir}
    install -m 0777 ${BINARY} ${D}${bindir}
    install -d ${D}${sysconfdir}/init.d
    cp init ${D}${sysconfdir}/init.d/${BINARY}
    chmod 755 ${D}${sysconfdir}/init.d/${BINARY}

    # This is needed because ldd entropy-tss reveals that our binary expects
    # to find ld-linux-x86-64.so.2 in /lib64
    ln -rs ${D}/lib ${D}/lib64
}

FILES:${PN} += "${bindir} /lib64"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

DEPENDS += " openssl"
RDEPENDS_${PN} += " libssl.so.3()(64bit)"
