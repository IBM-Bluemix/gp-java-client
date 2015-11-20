/*  
 * Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.g11n.pipeline.client;

import java.util.Map;
import java.util.Set;

import com.ibm.g11n.pipeline.client.impl.ServiceClientImpl;

/**
 * <code>ServiceClient</code> provides public REST API access to
 * IBM Globalization Pipeline service.
 * 
 * @author Yoshito Umaoka
 */
public abstract class ServiceClient {

    /**
     * Authentication scheme used for accessing Globalization Pipeline's
     * service endpoints.
     */
    public enum AuthScheme {
        /**
         * HMAC authentication. This authentication scheme is used
         * by default.
         */
        HMAC,
        /**
         * HTTP Basic authentication. This authentication scheme can
         * be used only for {@link UserType#READER READER} access.
         */
        BASIC
    };

    protected final ServiceAccount account;
    protected AuthScheme scheme = AuthScheme.HMAC;

    /**
     * Protected constructor for a subclass extending <code>ServiceClient</code>.
     * @param account   The service account.
     */
    protected ServiceClient(ServiceAccount account) {
        this.account = account;
    }

    /**
     * Returns an instance of ServiceClient for the specified ServiceAccount.
     * @param account   The service account.
     * @return  An instance of ServiceClient.
     * @throws IllegalArgumentException when account is null.
     */
    public static ServiceClient getInstance(ServiceAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("account must be specified.");
        }
        return new ServiceClientImpl(account);
    }

    /**
     * Returns an instance of ServiceClient. This factory method only works
     * if necessary account information is provided via environment variables
     * or VCAP_SERVICES on Bluemix.
     * @return An instance of ServiceClient, or null if sufficient configuration
     * is not provided in the runtime environment.
     */
    public static ServiceClient getInstance() {
        ServiceAccount account = ServiceAccount.getInstance();
        if (account == null) {
            return null;
        }
        return getInstance(account);
    }

    /**
     * Returns the service account used by this service client.
     * @return The service account used by this service client.
     */
    public ServiceAccount getServiceAccount() {
        return account;
    }

    /**
     * Returns the authentication scheme used for accessing IBM Globalization
     * Pipeline service's REST endpoints. By default, {@link AuthScheme#HMAC HMAC}
     * is used.
     * @return The authentication scheme.
     */
    public AuthScheme getAuthScheme() {
        return scheme;
    }

    /**
     * Sets the authentication scheme.
     * Note: {@link AuthScheme#BASIC BASIC} can be used only by
     * {@link UserType#READER READER} accounts.
     * @param scheme The authentication scheme.
     */
    public void setAuthScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }


    //
    // $service/v2 APIs
    //

    /**
     * Returns IBM Globalization Pipeline service's information.
     * @return The service information.
     * @throws ServiceException when the operation failed.
     */
    public abstract ServiceInfo getServiceInfo() throws ServiceException;


    //
    // {serviceInstanceId}/v2/bundles APIs
    //

    /**
     * Returns a set of bundle IDs available in the service instance.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * @return A set of bundle IDs.
     * @throws ServiceException when the operation failed.
     */
    public abstract Set<String> getBundleIds() throws ServiceException;

    /**
     * Creates a new translation bundle.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * @param bundleId
     *          The new bundle's ID. The bundle ID must match a regular expression pattern
     *          [a-zA-Z0-9][a-zA-Z0-9_.-]* and the length must be less than or equal
     *          to 255.
     * @param newBundleData
     *          The new bundle's configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract void createBundle(String bundleId,
            NewBundleData newBundleData) throws ServiceException;

    /**
     * Returns the bundle's configuration.
     * <p>
     * This operation is only allowed to all user types, but only
     * basic information (source language and target languages) is included
     * in the result if the requesting user type is {@link UserType#READER READER}.
     * of the service instance.
     * @param bundleId  The bundle ID.
     * @return          The bundle's configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract BundleData getBundleInfo(String bundleId) throws ServiceException;

    /**
     * Returns the bundle's metrics information.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * and {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * @param bundleId  The bundle ID.
     * @return          The bundle's metrics information.
     * @throws ServiceException when the operation failed.
     * @see LanguageMetrics
     */
    public abstract BundleMetrics getBundleMetrics(String bundleId) throws ServiceException;

    /**
     * Updates the bundle's configuration.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * @param bundleId  The bundle ID.
     * @param changeSet The change set of bundle configuration.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateBundle(String bundleId,
            BundleDataChangeSet changeSet) throws ServiceException;

    /**
     * Deletes the bundle.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * @param bundleId  The bundle ID.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteBundle(String bundleId) throws ServiceException;

    /**
     * Returns a map containing resource string key-value pairs in the specified
     * bundle and language.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param fallback  If the value in the source language is included when
     *                  translated value is not available.
     * @return          A map containing resource string key-value pairs.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, String> getResourceStrings(String bundleId,
            String language, boolean fallback) throws ServiceException;

    /**
     * Returns a map containing resource string entries indexed by resource key
     * in the bundle and the language.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @return          A map containing resource string entries indexed by resource key.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, ResourceEntryData> getResourceEntries(String bundleId,
            String language) throws ServiceException;


    /**
     * Returns per language metrics information
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * and {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @return          The language metrics information.
     * @throws ServiceException when the operation failed.
     */
    public abstract LanguageMetrics getLanguageMetrics(String bundleId,
            String language) throws ServiceException;

    /**
     * Uploads resource string key-value pairs.
     * <p>
     * Following restrictions are applied.
     * <ul>
     *  <li>Resource key must match a regular expression pattern [a-zA-Z0-9][a-zA-Z0-9_.-]*</li>
     *  <li>Length of resource key must be less than or equal to 255.</li>
     *  <li>Length of resource value must be less than or equal to 2047.</li>
     *  <li>Total number of key-value pairs after upload must be less than or equal to 500.</li>
     * </ul>
     * <p>
     * When the specified language is the source language of the bundle:
     * <ul>
     *  <li>A value for a new key will be sent to a machine translation and the translated
     *  value will be automatically added to the bundle's target languages.</li>
     *  <li>An existing key with a new value will be also sent to a machine translation
     *  and the translation will be automatically updated.</li>
     *  <li>An existing key with a same value won't affect translation target languages.</li>
     *  <li>A key not included in the input map will be deleted from the bundle.</li>
     * </ul>
     * <p>
     * When the specified language is not the source language of the bundle:
     * <ul>
     *  <li>If the language is currently not available in the bundle, the language
     *  will be added to the bundle's configuration.</li>
     *  <li>A new key which does not exist in the source language will be ignored.</li>
     *  <li>A key not included in the input map, but available in the source language will
     *  be automatically inserted to the specified language with a machine translated value.</li>
     * </ul>
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR}
     * of the service instance.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param strings   The resource string key-value pairs to be uploaded.
     * @throws ServiceException when the operation failed.
     */
    public abstract void uploadResourceStrings(String bundleId, String language,
            Map<String, String> strings) throws ServiceException;

    /**
     * Update resource string key-value pairs.
     * <p>
     * The same restrictions explained in {@link #uploadResourceStrings(String, String, Map)} are applied.
     * <p>
     * When the specified language is the source language of the bundle:
     * <ul>
     *  <li>A value for a new key will be sent to a machine translation and the translated
     *  value will be automatically added to the bundle's target languages.</li>
     *  <li>An existing key with a new value will be also sent to a machine translation
     *  and the translation will be automatically updated.</li>
     *  <li>An existing key with a same value won't affect translation target languages.</li>
     *  <li>A key not included in the input map will be ignored. This behavior is different
     *  from {@link #uploadResourceStrings(String, String, Map)}. </li>
     * </ul>
     * <p>
     * When the specified language is not the source language of the bundle:
     * <ul>
     *  <li>If the language is currently not available in the bundle, A {@link ServiceException}
     *  will be thrown. This is different from {@link #uploadResourceStrings(String, String, Map)}.</li>
     *  <li>A new key which does not exist in the source language will be ignored.</li>
     *  <li>If the argument <code>resync</code> is <code>true</code>, all key-value pairs in
     *  the language will be compared with the source language and update out of sync
     *  key-value pairs. This is useful when previous translation was failed by a service
     *  problem and you want to fix the problem.</li>
     * </ul>
     * <p>
     * Updating resource strings in the bundle's source language is only allowed to
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR} of the service instance.
     * Updating resource strings in a bundle's translation target language is
     * allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR}.
     * @param bundleId  The bundle ID
     * @param language  The language specified by BCP 47 language tag.
     * @param strings   The resource string key-value pairs to be uploaded.
     * @param resync    <code>true</code> to force the service to synchronize
     *                  resource string key-value pairs with the bundle's source
     *                  language. No effect if the specified language is the source
     *                  language of the bundle.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateResourceStrings(String bundleId, String language,
            Map<String, String> strings, boolean resync)
                    throws ServiceException;

    /**
     * Returns the resource entry specified by the bundle ID, the language and the resource key.
     * <p>
     * This operation is only allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR} of the service instance.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param resKey    The resource key.
     * @return          The resource entry data.
     * @throws ServiceException when the operation failed.
     */
    public abstract ResourceEntryData getResourceEntry(String bundleId,
            String language, String resKey) throws ServiceException;

    /**
     * Updates the resource entry.
     * <p>
     * Updating a resource entry in the bundle's source language is only allowed to
     * {@link UserType#ADMINISTRATOR ADMINISTRATOR} of the service instance.
     * Updating a resource entry in a bundle's translation target language is
     * allowed to {@link UserType#ADMINISTRATOR ADMINISTRATOR} and
     * {@link UserType#TRANSLATOR TRANSLATOR}.
     * @param bundleId  The bundle ID.
     * @param language  The language specified by BCP 47 language tag.
     * @param resKey    The resource key.
     * @param changeSet The change set of resource entry.
     * @throws ServiceException when the operation failed.
     */
    public abstract void updateResourceEntry(String bundleId, String language,
            String resKey, ResourceEntryDataChangeSet changeSet)
                    throws ServiceException;

    //
    // {serviceInstanceId}/v2/users APIs
    //

    /**
     * Returns a map containing user data objects in the service instance indexed
     * by user ID.
     * @return A map containing user data objects.
     * @throws ServiceException when the operation failed.
     */
    public abstract Map<String, UserData> getUsers() throws ServiceException;

    /**
     * Creates a new user.
     * <p>
     * This method returns <code>UserData</code> object with the generated user
     * ID and password. There is no way to access the password generated for the
     * new user later. A caller is responsible to store the password in a secure
     * place. If a password is lost, you can only request for a new password.
     * See {@link #updateUser(String, UserDataChangeSet, boolean)} about password
     * reset.
     * @param newUserData   The new user's configuration.
     * @return              The user data object created by this operation.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData createUser(NewUserData newUserData)
            throws ServiceException;

    /**
     * Returns the user data object.
     * <p>
     * Note: The password is not available in the returned user data object.
     * @param userId        The user ID.
     * @return              The user data object.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData getUser(String userId) throws ServiceException;

    /**
     * Updates the user data.
     * <p>
     * This operation allows you to reset password. When the argument <code>resetPassword</code>
     * is <code>true</code>, a new password will be set in the returned user data object.
     * Once password is reset, the old password cannot be used. When the argument
     * <code>resetPassword</code> is <code>false</code>, the user's password won't be returned
     * in the user data object.
     * @param userId        The user ID.
     * @param changeSet     The change set of user data.
     * @param resetPassword <code>true</code> to issue a new password.
     * @return              The user data object.
     * @throws ServiceException when the operation failed.
     */
    public abstract UserData updateUser(String userId,
            UserDataChangeSet changeSet, boolean resetPassword)
                    throws ServiceException;

    /**
     * Deletes the user.
     * @param userId        The user ID.
     * @throws ServiceException when the operation failed.
     */
    public abstract void deleteUser(String userId) throws ServiceException;
}
