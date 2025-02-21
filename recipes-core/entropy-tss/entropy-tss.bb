DESCRIPTION = "Copy binary to the image"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"
BINARY = "entropy-tss"
S = "${WORKDIR}"

INITSCRIPT_NAME = "${BINARY}"
INITSCRIPT_PARAMS = "defaults 99"

inherit update-rc.d

python () {
    entropy_tss_binary_uri = d.getVar('ENTROPY_TSS_BINARY_URI')

    if entropy_tss_binary_uri is None:
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            entropy_tss_binary_uri = origenv.getVar('ENTROPY_TSS_BINARY_URI')

    if entropy_tss_binary_uri:
        d.setVar('ENTROPY_TSS_BINARY_URI', entropy_tss_binary_uri)
    else:
        binary_name = d.getVar('BINARY')
        d.setVar('ENTROPY_TSS_BINARY_URI', "file://" + binary_name)

        bb.note("ENTROPY_TSS_BINARY_URI is set to: %s" % entropy_tss_binary_uri)
}

SRC_URI = "${ENTROPY_TSS_BINARY_URI} file://init"

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
