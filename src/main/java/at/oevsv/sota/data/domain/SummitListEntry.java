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

package at.oevsv.sota.data.domain;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDate;
import java.util.Objects;

/**
 * View of a summit with validity date ranges. This is built from a CSV file; since the library to process CSV does not
 * support records yet, we're forced to satisfy the bean contract.
 *
 * @author schwingenschloegl
 */
@Entity(name = "SummitListEntry")
@Table(name = "SummitList", indexes = {
        @Index(name = "idx_summitlistentry", columnList = "summitCode"),
        @Index(name = "idx_summitlistentry_validfrom", columnList = "validFrom"),
        @Index(name = "idx_summitlistentry_validto", columnList = "validTo")
})
@NamedQuery(name = "SummitList.updateValidFromAndValidToBySummitCode", query = "update SummitListEntry s set s.validFrom = :validFrom, s.validTo = :validTo where s.summitCode = :summitCode", lockMode = LockModeType.WRITE)
@NamedQuery(name = "SummitList.validSummit", query = "select s from SummitListEntry s where s.summitCode = :summitCode and s.validFrom >= :date and s.validTo <= :date")
public final class SummitListEntry extends PanacheEntityBase {

    @Id
    @Column(name = "summitCode")
    @CsvBindByName(column = "SummitCode")
    private String summitCode;

    @CsvBindByName(column = "SummitName")
    @Column(name = "summitName")
    private String summitName;

    @CsvBindByName(column = "ValidFrom")
    @CsvDate("dd/MM/yyyy")
    @Column(name = "validFrom")
    private LocalDate validFrom;

    @CsvBindByName(column = "ValidTo")
    @CsvDate("dd/MM/yyyy")
    @Column(name = "validTo")
    private LocalDate validTo;

    @Version
    private int version;

    public SummitListEntry() {
        // For bean contract.
    }

    public String getSummitCode() {
        return summitCode;
    }

    public void setSummitCode(String summitCode) {
        this.summitCode = summitCode;
    }

    public String getSummitName() {
        return summitName;
    }

    public void setSummitName(String summitName) {
        this.summitName = summitName;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public static boolean hasValidEntry(@Nullable Summit summit, @Nullable LocalDate date) {
        if (summit == null || date == null) {
            return false;
        }

        final var parameters = Parameters.with("summitCode", summit.code()).and("date", date);
        return find("s.summitCode = :summitCode and s.validFrom >= :date and s.validTo <= :date", parameters).firstResultOptional().isPresent();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SummitListEntry) obj;
        return Objects.equals(this.summitCode, that.summitCode) &&
                Objects.equals(this.summitName, that.summitName) &&
                Objects.equals(this.validFrom, that.validFrom) &&
                Objects.equals(this.validTo, that.validTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(summitCode, summitName, validFrom, validTo);
    }

    @Override
    public String toString() {
        return "SummitListEntry[" +
                "summitCode=" + summitCode + ", " +
                "summitName=" + summitName + ", " +
                "validFrom=" + validFrom + ", " +
                "validTo=" + validTo + ']';
    }
}
