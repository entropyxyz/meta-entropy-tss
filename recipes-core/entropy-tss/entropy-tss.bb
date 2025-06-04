SUMMARY = "Entropy CVM Image"
HOMEPAGE = "https://github.com/entropyxyz/entropy-core"
LICENSE = "CLOSED"
FILESEXTRAPATHS:prepend := "${THISDIR}:"

inherit cargo_bin update-rc.d

python () {
    cvm_service_name = d.getVar('CVM_SERVICE_NAME')

    if cvm_service_name is None:
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            cvm_service_name = origenv.getVar('CVM_SERVICE_NAME')

    if cvm_service_name:
        d.setVar('CVM_SERVICE_NAME', cvm_service_name)
        bb.note("CVM_SERVICE_NAME is set to: %s" % cvm_service_name)
    else:
        bb.fatal("CVM_SERVICE_NAME must be either `entropy-tss` or `api_key_tdx`")
        # bb.note("No CVM_SERVICE_NAME is set. Defaulting to entropy-tss")
        # d.setVar('CVM_SERVICE_NAME', 'entropy-tss')

    if cvm_service_name == "entropy-tss":
        bb.note("Building entropy-tss")
        d.setVar('SRC_URI', "git://github.com/entropyxyz/entropy-core.git;protocol=https;branch=master")
        d.setVar('SRCREV', "d3660102c9009fa96e309532a2e48f428f031840")
        d.setVar('EXTRA_CARGO_FLAGS', "-p entropy-tss")
        d.setVar('CARGO_FEATURES', "production")
    elif cvm_service_name == "api_key_tdx":
        bb.note("Building api_key_tdx")
        d.setVar('SRC_URI', "git://github.com/entropyxyz/api_key_tdx.git;protocol=https;branch=master")
        d.setVar('SRCREV', "dff00456221dfe48d12dc9af416e2bc456a51c78")
        d.setVar('EXTRA_CARGO_FLAGS', "")
    else:
        bb.fatal("CVM_SERVICE_NAME must be either `entropy-tss` or `api_key_tdx`")
}

INITSCRIPT_NAME = "${CVM_SERVICE_NAME}"
INITSCRIPT_PARAMS = "defaults 99"

# Remove build ID for better reproducibility
RUSTFLAGS += "-C link-arg=-Wl,--build-id=none"
# Use a consistent symbol mangling version
RUSTFLAGS += "-C symbol-mangling-version=v0"

# Disable incremental compilation for reproducibility
CARGO_PROFILE_RELEASE_INCREMENTAL = "false"

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

S = "${WORKDIR}/git"

SRC_URI += " file://init-${CVM_SERVICE_NAME}"

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    cp ${THISDIR}/init-${CVM_SERVICE_NAME} ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}
    chmod 755 ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}

    # This is needed because ldd entropy-tss reveals that our binary expects
    # to find ld-linux-x86-64.so.2 in /lib64
    ln -rs ${D}/lib ${D}/lib64
}

FILES:${PN} += " /lib64"
DEPENDS += " openssl"
RDEPENDS_${PN} += " libssl.so.3()(64bit)"
