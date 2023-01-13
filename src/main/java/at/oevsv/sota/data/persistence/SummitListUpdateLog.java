/*
 * Copyright (C) 2023 David Schwingenschlögl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.oevsv.sota.data.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Entity
@NamedQuery(name = "SummitListUpdateLog.findNewestAfter", query = "select s from SummitListUpdateLog s where s.date > :date order by s.date desc", lockMode = LockModeType.READ)
@NamedQuery(name = "SummitListUpdateLog.findNewest", query = "select s from SummitListUpdateLog s order by s.date desc", lockMode = LockModeType.READ)
@SuppressWarnings("java:S1104") // justification: managed panache entity; fields can be public!
public class SummitListUpdateLog extends PanacheEntity {

    private LocalDateTime date;
    private int updateCount;

    @Version
    private int version;

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Transactional
    public static LocalDateTime lastUpdate() {
        return find("#SummitListUpdateLog.findNewest").<SummitListUpdateLog>firstResultOptional().map(SummitListUpdateLog::getDate).orElse(null);
    }
}
