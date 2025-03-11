SUMMARY = "Entropy TSS"
HOMEPAGE = "https://github.com/entropyxyz/entropy-core"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"

inherit cargo_bin

INITSCRIPT_NAME = "entropy-tss"
INITSCRIPT_PARAMS = "defaults 99"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

SRC_URI = "git://github.com/entropyxyz/entropy-core.git;protocol=https;branch=peg/fix-tss-production-mode"
SRCREV="ecd7c932c8468939d866cf8887a5e4694ddcef51"
S = "${WORKDIR}/git"
EXTRA_CARGO_FLAGS = "-p entropy-tss"
CARGO_FEATURES = "production"

SRC_URI += " file://init"

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    cp init ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}
    chmod 755 ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}

    # This is needed because ldd entropy-tss reveals that our binary expects
    # to find ld-linux-x86-64.so.2 in /lib64
    ln -rs ${D}/lib ${D}/lib64
}

FILES:${PN} += " /lib64"
DEPENDS += " openssl"
RDEPENDS_${PN} += " libssl.so.3()(64bit)"
