/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.web.controller;

import lombok.NoArgsConstructor;
import org.openlmis.core.domain.ProcessingPeriod;
import org.openlmis.core.domain.Right;
import org.openlmis.core.domain.User;
import org.openlmis.core.exception.DataException;
import org.openlmis.rnr.domain.Comment;
import org.openlmis.rnr.domain.Rnr;
import org.openlmis.rnr.dto.RnrDTO;
import org.openlmis.rnr.search.criteria.RequisitionSearchCriteria;
import org.openlmis.rnr.service.RegimenColumnService;
import org.openlmis.rnr.service.RequisitionService;
import org.openlmis.rnr.service.RequisitionStatusChangeService;
import org.openlmis.rnr.service.RnrTemplateService;
import org.openlmis.web.response.OpenLmisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.openlmis.rnr.dto.RnrDTO.prepareForListApproval;
import static org.openlmis.rnr.dto.RnrDTO.prepareForView;
import static org.openlmis.rnr.service.RequisitionService.NUMBER_OF_PAGES;
import static org.openlmis.rnr.service.RequisitionService.SEARCH_ALL;
import static org.openlmis.web.response.OpenLmisResponse.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@NoArgsConstructor
public class RequisitionController extends BaseController {
  public static final String RNR = "rnr";
  public static final String RNR_SAVE_SUCCESS = "msg.rnr.save.success";
  public static final String RNR_LIST = "rnr_list";
  public static final String RNR_TEMPLATE = "rnr_template";

  public static final String PERIODS = "periods";
  public static final String CURRENCY = "currency";

  public static final String COMMENTS = "comments";
  public static final String REGIMEN_TEMPLATE = "regimen_template";
  public static final String LOSS_ADJUSTMENT_TYPES = "lossAdjustmentTypes";
  public static final String STATUS_CHANGES = "statusChanges";
  public static final String IS_EMERGENCY = "is_emergency";
  public static final String LOSSES_AND_ADJUSTMENT_TYPES = "lossesAndAdjustmentTypes";

  @Autowired
  private RequisitionService requisitionService;

  @Autowired
  private RnrTemplateService rnrTemplateService;

  @Autowired
  private RequisitionStatusChangeService requisitionStatusChangeService;

  @Autowired
  private RegimenColumnService regimenColumnService;

  private static final Logger logger = LoggerFactory.getLogger(RequisitionController.class);


  @RequestMapping(value = "/requisitions", method = POST, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> initiateRnr(@RequestParam("facilityId") Long facilityId,
                                                      @RequestParam("programId") Long programId,
                                                      @RequestParam("periodId") Long periodId,
                                                      @RequestParam("emergency") Boolean emergency,
                                                      HttpServletRequest request) {
    try {
      return response(RNR, requisitionService.initiate(facilityId, programId, periodId, loggedInUserId(request), emergency));
    } catch (DataException e) {
      return error(e, BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/requisitions", method = GET, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'VIEW_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> getRequisitionsForView(RequisitionSearchCriteria criteria, HttpServletRequest request) {
    criteria.setUserId(loggedInUserId(request));
    return response(RNR_LIST, prepareForView(requisitionService.get(criteria)));
  }


  @RequestMapping(value = "/requisitions/{id}/save", method = PUT, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> saveRnr(@RequestBody Rnr rnr,
                                                  @PathVariable("id") Long id,
                                                  HttpServletRequest request) {
    try {
      rnr.setId(id);
      rnr.setModifiedBy(loggedInUserId(request));
      requisitionService.save(rnr);
      return OpenLmisResponse.success(messageService.message(RNR_SAVE_SUCCESS));
    } catch (DataException e) {
      return OpenLmisResponse.error(e, BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/requisitions/{id}/submit", method = PUT, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> submit(@PathVariable("id") Long id,
                                                 HttpServletRequest request) {
    try {
      Rnr rnr = new Rnr(id);
      rnr.setModifiedBy(loggedInUserId(request));
      Rnr submittedRnr = requisitionService.submit(rnr);

      return success(messageService.message(requisitionService.getSubmitMessageBasedOnSupervisoryNode(submittedRnr.getFacility(),
        submittedRnr.getProgram())));
    } catch (DataException e) {
      return error(e, BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/requisitions/lossAndAdjustments/reference-data", method = GET, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal,'CREATE_REQUISITION, AUTHORIZE_REQUISITION, APPROVE_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> getReferenceData() {
    OpenLmisResponse referenceData = new OpenLmisResponse();
    referenceData.addData(LOSS_ADJUSTMENT_TYPES, requisitionService.getLossesAndAdjustmentsTypes());
    return referenceData.response(OK);
  }

  @RequestMapping(value = "/requisitions/{id}/authorize", method = PUT, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> authorize(@PathVariable("id") Long id,
                                                    HttpServletRequest request) {
    try {
      Rnr rnr = new Rnr(id);
      rnr.setModifiedBy(loggedInUserId(request));
      Rnr authorizedRnr = requisitionService.authorize(rnr);
      return success(messageService.message(requisitionService.getAuthorizeMessageBasedOnSupervisoryNode(
        authorizedRnr.getFacility(), authorizedRnr.getProgram())));
    } catch (DataException e) {
      return error(e, BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/requisitions/{id}/approve", method = PUT, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> approve(@PathVariable("id") Long id, HttpServletRequest request) {
    Rnr rnr = new Rnr(id);
    rnr.setModifiedBy(loggedInUserId(request));
    try {
      Rnr approvedRnr = requisitionService.approve(rnr);
      return success(messageService.message(requisitionService.getApproveMessageBasedOnParentNode(approvedRnr)));
    } catch (DataException dataException) {
      logger.warn("Error in approving requisition #{}", id, dataException);
      return error(dataException, BAD_REQUEST);
    }
  }


  @RequestMapping(value = "/requisitions-for-approval", method = GET, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal, 'APPROVE_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> listForApproval(HttpServletRequest request) {
    List<Rnr> requisitions = requisitionService.listForApproval(loggedInUserId(request));
    return response(RNR_LIST, prepareForListApproval(requisitions));
  }

  @RequestMapping(value = "/requisitions-for-convert-to-order", method = GET, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal, 'CONVERT_TO_ORDER')")
  public ResponseEntity<OpenLmisResponse> listForConvertToOrder(@RequestParam(value = "searchType", required = false, defaultValue = SEARCH_ALL) String searchType,
                                                                @RequestParam(value = "searchVal", required = false, defaultValue = "") String searchVal,
                                                                @RequestParam(value = "page", required = true, defaultValue = "1") Integer page,
                                                                @RequestParam(value = "sortBy", required = false, defaultValue = "submittedDate") String sortBy,
                                                                @RequestParam(value = "sortDirection", required = false, defaultValue = "asc") String sortDirection,
                                                                HttpServletRequest request)

  {
    try {
      Integer numberOfPages = requisitionService.getNumberOfPagesOfApprovedRequisitionsForCriteria(searchType, searchVal, loggedInUserId(request), Right.CONVERT_TO_ORDER);
      List<Rnr> approvedRequisitions = requisitionService.getApprovedRequisitionsForCriteriaAndPageNumber(
        searchType, searchVal, page, numberOfPages, loggedInUserId(request), Right.CONVERT_TO_ORDER, sortBy, sortDirection);
      List<RnrDTO> rnrDTOs = prepareForListApproval(approvedRequisitions);
      OpenLmisResponse response = new OpenLmisResponse(RNR_LIST, rnrDTOs);
      response.addData(NUMBER_OF_PAGES, numberOfPages);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return error(new DataException(e.getMessage()), NOT_FOUND);
    }
  }

  @RequestMapping(value = "/logistics/periods", method = GET, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal, 'CREATE_REQUISITION, AUTHORIZE_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> getAllPeriodsForInitiatingRequisitionWithRequisitionStatus(
    RequisitionSearchCriteria criteria, HttpServletRequest request) {

    criteria.setUserId(loggedInUserId(request));

    try {
      List<ProcessingPeriod> periodList = requisitionService.getProcessingPeriods(criteria);
      List<Rnr> requisitions = requisitionService.getRequisitionsFor(criteria, periodList);
      OpenLmisResponse response = new OpenLmisResponse(PERIODS, periodList);
      response.addData(RNR_LIST, requisitions);
      response.addData(IS_EMERGENCY, criteria.isEmergency());
      return new ResponseEntity<>(response, OK);
    } catch (DataException e) {
      return error(e, CONFLICT);
    }
  }

  @RequestMapping(value = "/requisitions/{id}", method = GET)
  @PostAuthorize("@requisitionPermissionService.hasPermission(principal, returnObject.body.data.get(\"rnr\"), 'VIEW_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> getById(@PathVariable Long id) {
    try {
      return response(RNR, requisitionService.getFullRequisitionById(id));
    } catch (DataException dataException) {
      return error(dataException, NOT_FOUND);
    }
  }

  @RequestMapping(value = "/requisitions/{id}/print", method = GET, headers = ACCEPT_PDF)
  @PostAuthorize("@requisitionPermissionService.hasPermission(principal, returnObject.model.get(\"rnr\"), 'VIEW_REQUISITION')")
  public ModelAndView printRequisition(@PathVariable Long id) {
    ModelAndView modelAndView = new ModelAndView("requisitionPDF");

    Rnr requisition = requisitionService.getFullRequisitionById(id);

    modelAndView.addObject(RNR, requisition);
    modelAndView.addObject(LOSSES_AND_ADJUSTMENT_TYPES, requisitionService.getLossesAndAdjustmentsTypes());

    Long programId = requisition.getProgram().getId();
    modelAndView.addObject(RNR_TEMPLATE, rnrTemplateService.fetchColumnsForRequisition(programId));
    modelAndView.addObject(REGIMEN_TEMPLATE, regimenColumnService.getRegimenColumnsForPrintByProgramId(programId));
    modelAndView.addObject(STATUS_CHANGES, requisitionStatusChangeService.getByRnrId(id));

    return modelAndView;
  }


  @RequestMapping(value = "/requisitions/{id}/comments", method = POST, headers = ACCEPT_JSON)
  @PreAuthorize("@permissionEvaluator.hasPermission(principal, 'CREATE_REQUISITION, AUTHORIZE_REQUISITION, APPROVE_REQUISITION')")
  public ResponseEntity<OpenLmisResponse> insertComment(@RequestBody Comment comment,
                                                        @PathVariable("id") Long id,
                                                        HttpServletRequest request) {
    comment.setRnrId(id);
    User author = new User();
    author.setId(loggedInUserId(request));
    comment.setAuthor(author);
    requisitionService.insertComment(comment);
    return OpenLmisResponse.response(COMMENTS, requisitionService.getCommentsByRnrId(id));
  }

  @RequestMapping(value = "/requisitions/{id}/comments", method = GET, headers = ACCEPT_JSON)
  public ResponseEntity<OpenLmisResponse> getCommentsForARnr(@PathVariable Long id) {
    return response(COMMENTS, requisitionService.getCommentsByRnrId(id));
  }
}
