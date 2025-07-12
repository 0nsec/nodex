package org.nodex.privategroup.invitation;
import org.nodex.api.nullsafety.NotNullByDefault;
@NotNullByDefault
interface ProtocolEngineFactory {
	ProtocolEngine<CreatorSession> createCreatorEngine();
	ProtocolEngine<InviteeSession> createInviteeEngine();
	ProtocolEngine<PeerSession> createPeerEngine();
}