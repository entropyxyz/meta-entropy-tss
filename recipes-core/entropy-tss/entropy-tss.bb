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


    cvm_service_src_rev = d.getVar('CVM_SERVICE_SRC_REV')

    if cvm_service_src_rev is None:
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            cvm_service_src_rev = origenv.getVar('CVM_SERVICE_SRC_REV')

    if cvm_service_src_rev:
        d.setVar('SRCREV', cvm_service_src_rev)
        bb.note("SRCREV is set to: %s" % cvm_service_src_rev)
    else:
        bb.fatal("CVM_SERVICE_SRC_REV must be set to the desired commit hash of the crate to build")

    cvm_service_src_branch = d.getVar('CVM_SERVICE_SRC_BRANCH')

    if cvm_service_src_branch is None:
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            cvm_service_src_branch = origenv.getVar('CVM_SERVICE_SRC_BRANCH')

    if cvm_service_src_branch:
        bb.note("Source branch is set to: %s" % cvm_service_src_branch)
        cvm_service_src_branch = ";branch=" + cvm_service_src_branch 
    else:
        d.setVar('SRC_URI', "git://github.com/entropyxyz/entropy-core.git;protocol=https")
        bb.note("Using default source branch")

    if cvm_service_name == "entropy-tss":
        bb.note("Building entropy-tss")
        d.setVar('SRC_URI', "git://github.com/entropyxyz/entropy-core.git;protocol=https" + cvm_service_src_branch)
        d.setVar('EXTRA_CARGO_FLAGS', "-p entropy-tss")
        d.setVar('CARGO_FEATURES', "production")
    elif cvm_service_name == "api_key_tdx":
        bb.note("Building api_key_tdx")
        d.setVar('SRC_URI', "git://github.com/entropyxyz/api_key_tdx.git;protocol=https" + cvm_service_src_branch)
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

# Python function to set SOURCE_DATE_EPOCH for reproducible builds
python do_set_source_date_epoch() {
    import subprocess
    import time

    # Get the commit date of the latest commit
    cmd = f"git -C {d.getVar('S')} log -1 --pretty=%ct"
    commit_date = subprocess.check_output(cmd, shell=True).decode('utf-8').strip()

    # Set SOURCE_DATE_EPOCH to the commit date
    d.setVar('SOURCE_DATE_EPOCH', commit_date)

    # Log the date for debugging
    human_date = time.strftime('%Y-%m-%d %H:%M:%S', time.gmtime(int(commit_date)))
    bb.note(f"Set SOURCE_DATE_EPOCH to {commit_date} ({human_date} UTC)")
}

# Add the source date epoch task to run after unpacking and before compiling
addtask set_source_date_epoch after do_unpack before do_compile

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
