/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.social.authentication.internal;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.social.authentication.SocialAuthConfiguration;
import org.xwiki.social.authentication.SocialAuthException;
import org.xwiki.social.authentication.SocialAuthSession;
import org.xwiki.social.authentication.SocialAuthenticationManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("socialAuth")
public class SocialAuthScriptService implements ScriptService
{

    @Inject
    private SocialAuthenticationManager socialAuthManager;

    @Inject
    private SocialAuthConfiguration socialAuthConfiguration;

    @Inject
    private Execution execution;

    public boolean ensureConnected(String provider)
    {
        if (!socialAuthManager.hasProvider(getContextUser(), provider)) {
            // Nothing to do, it's not going to work
            return false;
        }

        try {
            this.socialAuthManager.ensureConnected(provider);
            return true;
        } catch (SocialAuthException e) {
            return false;
        }
    }
    
    public boolean associateAccount(String provider)
    {
        if (socialAuthManager.hasProvider(getContextUser(), provider)) {
            // Nothing to do
            return false;
        }

        try {
            this.socialAuthManager.associateAccount(provider);
            return true;
        } catch (SocialAuthException e) {
            return false;
        }
    }

    public boolean getLoginButtonsEnabled()
    {
        return this.socialAuthConfiguration.getLoginButtonsEnabled();
    }

    public List<String> getAvailableProviders()
    {
        return this.socialAuthConfiguration.getAvailableProviders();
    }

    public Profile getSessionProfile()
    {
        if (this.socialAuthManager.getSession() == null) {
            return null;
        }
        return this.socialAuthManager.getSession().getProfile();
    }

    public boolean registerUser()
    {
        try {
            SocialAuthSession session = socialAuthManager.getSession();
            Map<String, String> parameters = SocialAuthUtil.getRequestParametersMap(getContext().getRequest());
            session.getProfile();
            this.socialAuthManager.createUser(parameters);
            return true;
        } catch (SocialAuthException e) {
            getContext().put("message", e.getMessage());
            return false;
        } catch (XWikiException e) {
            getContext().put("message", e.getMessage());
            return false;
        }
    }

    public boolean registerUser(String username)
    {
        try {
            SocialAuthSession session = socialAuthManager.getSession();
            Map<String, String> parameters = SocialAuthUtil.getRequestParametersMap(getContext().getRequest());
            session.getProfile();
            this.socialAuthManager.createUser(username, parameters);
            return true;
        } catch (SocialAuthException e) {
            getContext().put("message", e.getMessage());
            return false;
        } catch (XWikiException e) {
            getContext().put("message", e.getMessage());
            return false;
        }
    }

    public boolean hasProvider(String provider)
    {
        return this.socialAuthManager.hasProvider(getContextUser(), provider);
    }

    public boolean isConnected(String provider)
    {
        return this.socialAuthManager.isConnected(provider);
    }

    public String getToken(String provider)
    {
        SocialAuthSession session = this.socialAuthManager.getSession();
        if (session == null || session.getAuthProvider(provider) == null) {
            return null;
        }
        return session.getAuthProvider(provider).getAccessGrant().getKey();
    }

    // /////////////////////////////////////////////////////////////////////////

    private DocumentReference getContextUser()
    {
        return this.getContext().getUserReference();
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
