/*******************************************************************************
 * Copyright (c) 1999-2010, Vodafone Group Services
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above 
 *       copyright notice, this list of conditions and the following 
 *       disclaimer in the documentation and/or other materials provided 
 *       with the distribution.
 *     * Neither the name of Vodafone Group Services nor the names of its 
 *       contributors may be used to endorse or promote products derived 
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 ******************************************************************************/
package com.wayfinder.core.search.internal;

import com.wayfinder.core.CallbackHandler;
import com.wayfinder.core.network.internal.mc2.MC2RequestListener;
import com.wayfinder.core.search.onelist.MatchDetailsRequestListener;
import com.wayfinder.core.shared.RequestID;
import com.wayfinder.core.shared.error.CoreError;
import com.wayfinder.core.shared.internal.poidetails.PoiDetailImpl;

/**
 * <p>Bridge between {@link MatchDetailsMC2Request} and Application/UI. 
 * As the core don't take any action on this the result are just passed over 
 * to the caller (Application/UI)</p>
 * <p>Get the result of a match details request via 
 * {@link MC2RequestListener} and pass it to the given 
 * {@link MatchDetailsRequestListener} asynchronous trough {@link CallbackHandler}.</p>
 * 
 * 
 */
public class MatchDetailsResponseHandler implements Runnable, MC2RequestListener {
    private final RequestID m_requestID;
    private final MatchDetailsRequestListener m_listener;
    private final OneListSearchMatchImpl m_searchMatch;
    private final CallbackHandler m_callhandler;

    private PoiDetailImpl m_result;
    
    
    private CoreError m_error;
    
    /**
     * @param requestID the {@link RequestID} of the request
     * @param listener the Application/UI listener
     * @param callhandler the {@link CallbackHandler} used to post the listener 
     * notification
     */
    public MatchDetailsResponseHandler(RequestID requestID, OneListSearchMatchImpl searchMatch,
            MatchDetailsRequestListener listener, CallbackHandler callhandler) {
        m_requestID = requestID;
        m_listener = listener;
        m_callhandler = callhandler;
        m_searchMatch = searchMatch;
    }
    
    /**
     * @param result a {@link PoiDetailImpl) or null if there was an error 
     * @param error a {@link CoreError} if the request has failed, 
     * null otherwise
     * 
     * @see com.wayfinder.core.network.internal.mc2.MC2RequestListener#requestDone(java.lang.Object, com.wayfinder.core.shared.error.CoreError) 
     */
    public void requestDone(Object result, CoreError error) {
        m_result = (PoiDetailImpl)result;
        m_error = error;
        m_callhandler.callInvokeCallbackRunnable(this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (m_error != null) {
            m_listener.error(m_requestID, m_error);
        } else {
            //update of the SearchMatch is done from ui thread so we don't 
            //have synchronization issues
            m_searchMatch.setFullInfo(m_result);
            m_listener.matchDetailsUpdated(m_requestID, m_searchMatch);
        }
    }
}
