# Disable dropbear on all runlevels
INITSCRIPT_PARAMS = "stop 10 S ."
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://dropbear.default"

python () {
    ssh_pubkey = d.getVar('SSH_PUBKEY')

    if ssh_pubkey is None:
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            ssh_pubkey = origenv.getVar('SSH_PUBKEY')

    if ssh_pubkey:
        d.setVar('SSH_PUBKEY', ssh_pubkey)
        bb.note("SSH_PUBKEY is set to: %s" % ssh_pubkey)
    else:
        bb.note("No SSH_PUBKEY is set. The built image will have no SSH access!")
}

do_install:append() {
    install -d ${D}${sysconfdir}/default
    # override default poky dropbear configurations with local dropbear.default file
    install -m 0644 ${WORKDIR}/dropbear.default ${D}${sysconfdir}/default/dropbear

    # Ensure proper permissions on dropbear directory
    install -d ${D}${sysconfdir}/dropbear
    chmod 700 ${D}${sysconfdir}/dropbear

    # Create both possible directories for keys
    install -d ${D}/home/root/.ssh

    # Inject the SSH public key into the image
    echo "${SSH_PUBKEY}" > ${D}/home/root/.ssh/authorized_keys

    # Set proper permissions
    chmod 600 ${D}/home/root/.ssh/authorized_keys
}

FILES:${PN} += "\
    ${sysconfdir}/default/dropbear \
    /home/root/.ssh \
    /home/root/.ssh/authorized_keys \
"
