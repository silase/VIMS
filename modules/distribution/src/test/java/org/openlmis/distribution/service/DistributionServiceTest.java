/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.distribution.service;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.db.categories.UnitTests;
import org.openlmis.distribution.domain.Distribution;
import org.openlmis.distribution.domain.FacilityDistributionData;
import org.openlmis.distribution.domain.FacilityVisit;
import org.openlmis.distribution.domain.FacilityVisit;
import org.openlmis.distribution.repository.DistributionRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class DistributionServiceTest {

  @InjectMocks
  DistributionService service;

  @Mock
  FacilityVisitService facilityVisitService;

  @Mock
  DistributionRepository repository;

  @Test
  public void shouldCreateDistribution() throws Exception {
    Distribution distribution = new Distribution();
    Distribution expectedDistribution = new Distribution();
    when(repository.create(distribution)).thenReturn(expectedDistribution);

    Distribution initiatedDistribution = service.create(distribution);

    verify(repository).create(distribution);
    assertThat(initiatedDistribution, is(expectedDistribution));

  }

  @Test
  public void shouldSyncFacilityDistributionData() {
    Long distributionId = 1l;
    Long facilityId = 1l;
    FacilityDistributionData facilityDistributionData = mock(FacilityDistributionData.class);
    FacilityVisit facilityVisit = mock(FacilityVisit.class);
    when(facilityDistributionData.getFacilityId()).thenReturn(facilityId);
    when(facilityDistributionData.getFacilityVisit()).thenReturn(facilityVisit);

    service.sync(distributionId, facilityDistributionData);

    verify(facilityVisit).setDistributionId(distributionId);
    verify(facilityVisit).setFacilityId(facilityId);
    verify(facilityVisitService).save(facilityDistributionData.getFacilityVisit());
  }

  @Test
  public void itShouldGetDistributionIfExists() throws Exception {
    service.get(new Distribution());

    verify(repository).get(new Distribution());
  }
}
