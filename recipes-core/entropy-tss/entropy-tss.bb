SUMMARY = "Entropy TSS"
HOMEPAGE = "https://github.com/entropyxyz/entropy-core"
LICENSE = "CLOSED"

inherit cargo_bin

# Enable network for the compile task allowing cargo to download dependencies
do_compile[network] = "1"

SRC_URI = "git://github.com/entropyxyz/entropy-core.git;protocol=https;branch=master"
SRCREV="1a04c4d37c8ce87ee3d737f75e24a84ed9729245"
S = "${WORKDIR}/git"
EXTRA_CARGO_FLAGS = "-p entropy-tss"
