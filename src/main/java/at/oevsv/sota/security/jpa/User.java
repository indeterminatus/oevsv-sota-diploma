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

package at.oevsv.sota.security.jpa;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User entity used for JPA-based authorization.
 *
 * @implNote Note that a table "User" is problematic, as this is a reserved word in some DBMS. H2 for example completely
 * misses quotes in auto-generated statements. The safest bet was to use another table name.
 */
@Entity(name = "User")
@Table(name = "UserTable")
@UserDefinition
public class User extends PanacheEntityBase {

    @Id
    @Username
    @Column(name = "username")
    public String username;

    @Password
    public String password;

    @Roles
    public String role;

    static void add(String username, String password, String roles) {
        User user = new User();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(password);
        user.role = roles;
        user.persist();
    }

    static void changePassword(String username, String password) {
        User user = findById(username);
        if (user != null) {
            user.password = BcryptUtil.bcryptHash(password);
            user.persist();
        }
    }
}
