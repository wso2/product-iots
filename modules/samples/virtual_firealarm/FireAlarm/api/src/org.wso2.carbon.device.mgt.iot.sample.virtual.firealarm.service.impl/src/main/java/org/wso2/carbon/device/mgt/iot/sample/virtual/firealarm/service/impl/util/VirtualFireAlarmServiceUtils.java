package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception
        .VirtualFireAlarmEnrollmentException;

public class VirtualFireAlarmServiceUtils {
    private static final Log log = LogFactory.getLog(VirtualFireAlarmServiceUtils.class);

    public static CertificateManagementService getCertificateManagementService() throws
                                                                                 VirtualFireAlarmEnrollmentException {

        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        CertificateManagementService certificateManagementService = (CertificateManagementService)
                ctx.getOSGiService(CertificateManagementService.class, null);

        if (certificateManagementService == null) {
            String msg = "EnrollmentService is not initialized";
            log.error(msg);
            throw new VirtualFireAlarmEnrollmentException(msg);
        }

        return certificateManagementService;
    }

}
