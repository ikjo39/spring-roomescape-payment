package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.PaymentCredentialMissMatchException;
import roomescape.payment.dto.PaymentConfirmResponse;
import roomescape.payment.dto.SavePaymentCredentialRequest;
import roomescape.payment.infrastructure.PaymentGateway;
import roomescape.payment.model.PaymentCredential;
import roomescape.payment.model.PaymentHistory;
import roomescape.payment.repository.PaymentCredentialRepository;
import roomescape.payment.repository.PaymentHistoryRepository;
import roomescape.reservation.model.Reservation;

@Service
public class PaymentService {

    private final PaymentCredentialRepository paymentCredentialRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            final PaymentCredentialRepository paymentCredentialRepository,
            final PaymentHistoryRepository paymentHistoryRepository,
            final PaymentGateway paymentGateway
    ) {
        this.paymentCredentialRepository = paymentCredentialRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.paymentGateway = paymentGateway;
    }

    public void saveCredential(final SavePaymentCredentialRequest request) {
        final PaymentCredential paymentCredential = request.toPaymentCredential();
        paymentCredentialRepository.save(paymentCredential);
    }

    @Transactional
    public void submitPayment(
            final String orderId,
            final Long amount,
            final String paymentKey,
            final Reservation reservation
    ) {
        matchPaymentCredential(orderId, amount);

        final PaymentConfirmResponse confirmResponse = paymentGateway.confirm(orderId, amount, paymentKey);
        final PaymentHistory paymentHistory = confirmResponse.toPaymentHistory(reservation);
        paymentHistoryRepository.save(paymentHistory);
        paymentCredentialRepository.deleteAllByOrderIdAndAmount(orderId, amount);
    }

    private void matchPaymentCredential(final String orderId, final Long amount) {
        final boolean isMatch = paymentCredentialRepository.existsByOrderIdAndAmount(orderId, amount);
        if (!isMatch) {
            throw new PaymentCredentialMissMatchException("결제 정보가 유효하지 않습니다.");
        }
    }
}
