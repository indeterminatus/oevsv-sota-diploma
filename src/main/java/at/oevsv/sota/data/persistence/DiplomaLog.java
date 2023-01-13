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

import at.oevsv.sota.data.api.Candidate;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDate;

/**
 * Keeping a log of requested/awarded diploma. This should contain everything needed to re-create a specific diploma,
 * if need be.
 *
 * @author schwingenschloegl
 */
@Entity(name = "DiplomaLog")
@SequenceGenerator(initialValue = 1, name = "diploma", sequenceName = "diploma_sequence")
@Table(name = "DiplomaLog")
public class DiplomaLog extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "diploma", strategy = GenerationType.SEQUENCE)
    public Long id;

    @Column(name = "reviewMailSent")
    private boolean reviewMailSent;

    @Column(name = "callSign", length = 15)
    private String callSign;

    @Column(name = "mail", length = 100)
    private String mail;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "category", length = 15)
    @Enumerated(EnumType.STRING)
    private Candidate.Category category;

    @Column(name = "rank", length = 10)
    @Enumerated(EnumType.STRING)
    private Candidate.Rank rank;

    @Column(name = "creationDate")
    private LocalDate creationDate;

    @Column(name = "OE1")
    private int activationsOE1;

    @Column(name = "OE2")
    private int activationsOE2;

    @Column(name = "OE3")
    private int activationsOE3;

    @Column(name = "OE4")
    private int activationsOE4;

    @Column(name = "OE5")
    private int activationsOE5;

    @Column(name = "OE6")
    private int activationsOE6;

    @Column(name = "OE7")
    private int activationsOE7;

    @Column(name = "OE8")
    private int activationsOE8;

    @Column(name = "OE9")
    private int activationsOE9;

    @Version
    private int version;

    public void setReviewMailSent(boolean reviewMailSent) {
        this.reviewMailSent = reviewMailSent;
    }

    public boolean isReviewMailSent() {
        return reviewMailSent;
    }

    public String getCallSign() {
        return callSign;
    }

    public String getMail() {
        return mail;
    }

    public String getName() {
        return name;
    }

    public Candidate.Category getCategory() {
        return category;
    }

    public Candidate.Rank getRank() {
        return rank;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getActivationsOE1() {
        return activationsOE1;
    }

    public int getActivationsOE2() {
        return activationsOE2;
    }

    public int getActivationsOE3() {
        return activationsOE3;
    }

    public int getActivationsOE4() {
        return activationsOE4;
    }

    public int getActivationsOE5() {
        return activationsOE5;
    }

    public int getActivationsOE6() {
        return activationsOE6;
    }

    public int getActivationsOE7() {
        return activationsOE7;
    }

    public int getActivationsOE8() {
        return activationsOE8;
    }

    public int getActivationsOE9() {
        return activationsOE9;
    }

    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Candidate.Category category) {
        this.category = category;
    }

    public void setRank(Candidate.Rank rank) {
        this.rank = rank;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setActivationsOE1(int activationsOE1) {
        this.activationsOE1 = activationsOE1;
    }

    public void setActivationsOE2(int activationsOE2) {
        this.activationsOE2 = activationsOE2;
    }

    public void setActivationsOE3(int activationsOE3) {
        this.activationsOE3 = activationsOE3;
    }

    public void setActivationsOE4(int activationsOE4) {
        this.activationsOE4 = activationsOE4;
    }

    public void setActivationsOE5(int activationsOE5) {
        this.activationsOE5 = activationsOE5;
    }

    public void setActivationsOE6(int activationsOE6) {
        this.activationsOE6 = activationsOE6;
    }

    public void setActivationsOE7(int activationsOE7) {
        this.activationsOE7 = activationsOE7;
    }

    public void setActivationsOE8(int activationsOE8) {
        this.activationsOE8 = activationsOE8;
    }

    public void setActivationsOE9(int activationsOE9) {
        this.activationsOE9 = activationsOE9;
    }
}
