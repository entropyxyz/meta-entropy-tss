SUMMARY = "Entropy TSS"
HOMEPAGE = "https://github.com/entropyxyz/entropy-core"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"

inherit cargo_bin

INITSCRIPT_NAME = "entropy-tss"
INITSCRIPT_PARAMS = "defaults 99"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

SRC_URI = "git://github.com/entropyxyz/entropy-core.git;protocol=https;branch=master"
SRCREV="1a04c4d37c8ce87ee3d737f75e24a84ed9729245"
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
