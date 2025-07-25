package org.nodex.android.contact.add.nearby;
import android.graphics.Bitmap;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.qrcode.QrCodeClassifier.QrCodeType;
import androidx.annotation.Nullable;
abstract class AddContactState {
	static class KeyAgreementListening extends AddContactState {
		final Bitmap qrCode;
		KeyAgreementListening(Bitmap qrCode) {
			this.qrCode = qrCode;
		}
	}
	static class QrCodeScanned extends AddContactState {
	}
	static class KeyAgreementWaiting extends AddContactState {
	}
	static class KeyAgreementStarted extends AddContactState {
	}
	static class ContactExchangeStarted extends AddContactState {
	}
	static class ContactExchangeFinished extends AddContactState {
		final ContactExchangeResult result;
		ContactExchangeFinished(ContactExchangeResult result) {
			this.result = result;
		}
	}
	static class Failed extends AddContactState {
		static class WrongQrCodeType extends Failed {
			final QrCodeType qrCodeType;
			WrongQrCodeType(QrCodeType qrCodeType) {
				this.qrCodeType = qrCodeType;
			}
		}
		static class WrongQrCodeVersion extends Failed {
			final boolean qrCodeTooOld;
			WrongQrCodeVersion(boolean qrCodeTooOld) {
				this.qrCodeTooOld = qrCodeTooOld;
			}
		}
	}
	abstract static class ContactExchangeResult {
		static class Success extends ContactExchangeResult {
			final Author remoteAuthor;
			Success(Author remoteAuthor) {
				this.remoteAuthor = remoteAuthor;
			}
		}
		static class Error extends ContactExchangeResult {
			@Nullable
			final Author duplicateAuthor;
			Error(@Nullable Author duplicateAuthor) {
				this.duplicateAuthor = duplicateAuthor;
			}
		}
	}
}