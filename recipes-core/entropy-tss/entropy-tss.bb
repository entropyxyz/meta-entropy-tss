DESCRIPTION = "Compile entropy-tss"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"
BINARY = "entropy-tss"
S = "${WORKDIR}"

INITSCRIPT_NAME = "${BINARY}"
INITSCRIPT_PARAMS = "defaults 99"

inherit cargo-bin update-rc.d

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

SRC_URI = "git://github.com/entropy-xyz/entropy-core.git;branch=main"
SRCREV = "1a04c4d37c8ce87ee3d737f75e24a84ed9729245"

SRC_URI += "file://init"

S = "${WORKDIR}/git"

EXTRA_CARGO_FLAGS = "-p entropy-tss"
CARGO_FEATURES = "production"

do_install:append() {
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
