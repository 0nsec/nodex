package org.nodex.privategroup.invitation;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
interface ProtocolEngineFactory {
	ProtocolEngine<CreatorSession> createCreatorEngine();
	ProtocolEngine<InviteeSession> createInviteeEngine();
	ProtocolEngine<PeerSession> createPeerEngine();
}