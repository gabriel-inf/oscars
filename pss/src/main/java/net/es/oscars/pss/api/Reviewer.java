package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.beans.review.ReviewResult;
import net.es.oscars.pss.beans.review.ReviewSpecification;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.enums.ActionType;

import java.util.Map;

/**
 * Created by haniotak on 1/13/16.
 */
public interface Reviewer {

    ReviewResult reviewStatus(ReviewSpecification reviewSpec) throws PSSException;
}
