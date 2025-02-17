PACKAGE_INSTALL:append = " entropy-tss"
IMAGE_INSTALL:append = " kvdb-storage"

python () {
    # Check if DEBUG_TWEAKS_ENABLED is set in the environment or in local.conf
    debug_tweaks_enabled = d.getVar('DEBUG_TWEAKS_ENABLED')

    if debug_tweaks_enabled is None:
        # If not set, check the original environment
        origenv = d.getVar("BB_ORIGENV", False)
        if origenv:
            debug_tweaks_enabled = origenv.getVar('DEBUG_TWEAKS_ENABLED')

    if debug_tweaks_enabled:
        # If DEBUG_TWEAKS_ENABLED is set (to any non-empty value), keep its value
        d.setVar('DEBUG_TWEAKS_ENABLED', debug_tweaks_enabled)
    else:
        # If DEBUG_TWEAKS_ENABLED is not set, set it to '1' by default
        d.setVar('DEBUG_TWEAKS_ENABLED', '0')

    # set the image features based on the value of DEBUG_TWEAKS_ENABLED
    if d.getVar('DEBUG_TWEAKS_ENABLED') == '1':
        # give a warning that the debug tweaks are enabled
        bb.warn("Debug tweaks are enabled in the image")
        # add the debug-tweaks feature to the image if DEBUG_TWEAKS_ENABLED is set
        d.appendVar('IMAGE_FEATURES', ' debug-tweaks')
        # add dropbear to the package install list to be able to login for debugging purposes
        d.appendVar('PACKAGE_INSTALL', ' dropbear')
}
