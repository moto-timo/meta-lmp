UBOOT_VERSION = "v2022.01"

UBRANCH = "xilinx-v2022.01-bsp"
UBOOTURI = "git://github.com/foundriesio/u-boot.git;protocol=https"

SRCREV = "d66b7543afe096065f99bd0c28f0627eaf41e4ad"

include recipes-bsp/u-boot/u-boot-xlnx.inc
include recipes-bsp/u-boot/u-boot-spl-zynq-init.inc
include recipes-bsp/u-boot/u-boot-lmp-common.inc

PROVIDES += "u-boot"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://README;beginline=1;endline=4;md5=744e7e3bb0c94b4b9f6b3db3bf893897"

# u-boot-xlnx has support for these
HAS_PLATFORM_INIT ?= " \
		xilinx_zynqmp_virt_config \
		xilinx_zynq_virt_defconfig \
		xilinx_versal_vc_p_a2197_revA_x_prc_01_revA \
		"

# add pmu-firmware and fpga bitstream (loading FPGA from SPL) dependancies
do_compile[depends] += "virtual/pmu-firmware:do_deploy virtual/bitstream:do_deploy"

SRC_URI:append = " \
    file://fw_env.config \
    ${@bb.utils.contains('MACHINE_FEATURES', 'ebbr', 'file://lmp-ebbr.cfg', 'file://lmp.cfg', d)} \
"

# LMP base requires a different u-boot configuration fragment
SRC_URI:append:lmp-base = " file://lmp-base.cfg "
SRC_URI:remove:lmp-base = "file://lmp.cfg"

SRC_URI:append:uz = " \
    file://pm_cfg_obj.c \
"

# copy platform files to u-boot source dir
do_configure:prepend() {
    if [ -n "${PLATFORM_INIT_DIR}" ]; then
        for i in ${PLATFORM_INIT_FILES}; do
            cp ${PLATFORM_INIT_STAGE_DIR}/$i ${S}/
        done
    fi
}

# generate and configure u-boot to use pm_cfg_obj.bin
do_compile:prepend:uz() {
    ${PYTHON} ${S}/tools/zynqmp_pm_cfg_obj_convert.py ${WORKDIR}/pm_cfg_obj.c ${S}/pm_cfg_obj.bin
    echo "CONFIG_ZYNQMP_SPL_PM_CFG_OBJ_FILE=\"${S}/pm_cfg_obj.bin\"" >> ${B}/.config
}

# Support additional u-boot classes such as u-boot-fitimage
UBOOT_CLASSES ?= ""
LOCALVERSION = "+xlnx"
inherit ${UBOOT_CLASSES} fio-u-boot-localversion
