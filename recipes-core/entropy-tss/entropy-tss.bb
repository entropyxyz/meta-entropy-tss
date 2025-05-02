SUMMARY = "Entropy TSS"
HOMEPAGE = "https://github.com/entropyxyz/entropy-core"
LICENSE = "AGPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=eb1e647870add0502f8f010b19de32af"
FILESEXTRAPATHS:prepend := "${THISDIR}:"

inherit cargo_bin update-rc.d

INITSCRIPT_NAME = "entropy-tss"
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

SRC_URI = "git://github.com/entropyxyz/entropy-core.git;protocol=https;branch=master"
SRCREV="d3660102c9009fa96e309532a2e48f428f031840"
S = "${WORKDIR}/git"
EXTRA_CARGO_FLAGS = "-p entropy-tss"
CARGO_FEATURES = "production"

SRC_URI += " file://init"

do_install:append() {
    install -d ${D}${sysconfdir}/init.d
    cp ${THISDIR}/init ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}
    chmod 755 ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}

    # This is needed because ldd entropy-tss reveals that our binary expects
    # to find ld-linux-x86-64.so.2 in /lib64
    ln -rs ${D}/lib ${D}/lib64
}

FILES:${PN} += " /lib64"
DEPENDS += " openssl"
RDEPENDS_${PN} += " libssl.so.3()(64bit)"
