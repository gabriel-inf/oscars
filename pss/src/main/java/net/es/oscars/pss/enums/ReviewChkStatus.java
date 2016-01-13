package net.es.oscars.pss.enums;

public enum ReviewChkStatus {
    REV_CONFIGURED {
        ReviewStatus asReviewStatus() {
            return ReviewStatus.REV_CONFIGURED;
        }
    },
    REV_CLEANED {
        ReviewStatus asReviewStatus() {
            return ReviewStatus.REV_CLEANED;
        }
    }

}
