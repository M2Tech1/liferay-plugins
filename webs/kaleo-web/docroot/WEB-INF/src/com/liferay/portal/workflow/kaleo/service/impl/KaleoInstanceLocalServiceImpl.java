/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.service.impl;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.service.base.KaleoInstanceLocalServiceBaseImpl;
import com.liferay.portal.workflow.kaleo.util.WorkflowContextUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <a href="KaleoInstanceLocalServiceImpl.java.html"><b><i>View Source</i></b>
 * </a>
 *
 * @author Brian Wing Shun Chan
 */
public class KaleoInstanceLocalServiceImpl
	extends KaleoInstanceLocalServiceBaseImpl {

	public KaleoInstance addKaleoInstance(
			long kaleoDefinitionId, String kaleoDefinitionName,
			int kaleoDefinitionVersion, Map<String, Serializable> context,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(
			serviceContext.getUserId());
		Date now = new Date();

		long kaleoInstanceId = counterLocalService.increment();

		KaleoInstance kaleoInstance =
			kaleoInstancePersistence.create(kaleoInstanceId);

		kaleoInstance.setCompanyId(user.getCompanyId());
		kaleoInstance.setUserId(user.getUserId());
		kaleoInstance.setUserName(user.getFullName());
		kaleoInstance.setCreateDate(now);
		kaleoInstance.setModifiedDate(now);
		kaleoInstance.setKaleoDefinitionId(kaleoDefinitionId);
		kaleoInstance.setKaleoDefinitionName(kaleoDefinitionName);
		kaleoInstance.setKaleoDefinitionVersion(kaleoDefinitionVersion);
		kaleoInstance.setCompleted(false);
		kaleoInstance.setContext(WorkflowContextUtil.convert(context));

		kaleoInstancePersistence.update(kaleoInstance, false);

		return kaleoInstance;
	}

	public KaleoInstance completeKaleoInstance(long kaleoInstanceId)
		throws PortalException, SystemException {

		KaleoInstance kaleoInstance = kaleoInstancePersistence.findByPrimaryKey(
			kaleoInstanceId);

		kaleoInstance.setCompleted(true);
		kaleoInstance.setCompletionDate(new Date());

		kaleoInstancePersistence.update(kaleoInstance, false);

		return kaleoInstance;
	}

	public List<KaleoInstance> getKaleoInstances(
			String kaleoDefinitionName, int kaleoDefinitionVersion,
			boolean completed, int start, int end,
			OrderByComparator orderByComparator, ServiceContext serviceContext)
		throws SystemException {

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			KaleoInstance.class);

		dynamicQuery.add(
			PropertyFactoryUtil.forName("companyId").eq(
				serviceContext.getCompanyId()));
		dynamicQuery.add(
			PropertyFactoryUtil.forName("kaleoDefinitionName").eq(
				kaleoDefinitionName));
		dynamicQuery.add(
			PropertyFactoryUtil.forName("kaleoDefinitionVersion").eq(
				kaleoDefinitionVersion));

		if (completed) {
			dynamicQuery.add(
				PropertyFactoryUtil.forName("completionDate").isNotNull());
		}
		else {
			dynamicQuery.add(
				PropertyFactoryUtil.forName("completionDate").isNull());
		}

		List<Object> results = dynamicQuery(
			dynamicQuery, start, end, orderByComparator);

		return toKaleoInstances(results);
	}

	public int getKaleoInstancesCount(
			String kaleoDefinitionName, int kaleoDefinitionVersion,
			boolean completed, ServiceContext serviceContext)
		throws SystemException {

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			KaleoInstance.class);

		dynamicQuery.add(
			PropertyFactoryUtil.forName("companyId").eq(
				serviceContext.getCompanyId()));
		dynamicQuery.add(
			PropertyFactoryUtil.forName("kaleoDefinitionName").eq(
				kaleoDefinitionName));
		dynamicQuery.add(
			PropertyFactoryUtil.forName("kaleoDefinitionVersion").eq(
				kaleoDefinitionVersion));

		if (completed) {
			dynamicQuery.add(
				PropertyFactoryUtil.forName("completionDate").isNotNull());
		}
		else {
			dynamicQuery.add(
				PropertyFactoryUtil.forName("completionDate").isNull());
		}

		return dynamicQueryCount(dynamicQuery);
	}

	public KaleoInstance updateKaleoInstance(
			long kaleoInstanceId, Map<String, Serializable> context,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		KaleoInstance kaleoInstance = kaleoInstancePersistence.findByPrimaryKey(
			kaleoInstanceId);

		kaleoInstance.setContext(WorkflowContextUtil.convert(context));

		return kaleoInstance;
	}

	protected List<KaleoInstance> toKaleoInstances(List<Object> results) {
		List<KaleoInstance> kaleoInstances =
			new ArrayList<KaleoInstance>(results.size());

		for (Object result : results) {
			kaleoInstances.add((KaleoInstance)result);
		}

		return kaleoInstances;
	}

}