package m3da.server.tcp;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sierrawireless.airvantage.tech.awtda3codec.AwtDa3CodecService;
import com.sierrawireless.airvantage.tech.awtda3codec.DecoderOutput;
import com.sierrawireless.airvantage.tech.awtda3codec.EnvelopeDecoder;
import com.sierrawireless.airvantage.tech.awtda3codec.EnvelopeEncoder;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3Envelope;

/**
 * {@link IoFilter} in charge of decoding the M3DA envelopes. It decodes received {@link ByteBuffer} into
 * {@link AwtDa3Envelope} (with accumulation). It encodes sent {@link AwtDa3Envelope} into {@link ByteBuffer}.
 */
public class EnvelopeFilter extends IoFilterAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(EnvelopeFilter.class);

    private static final String ENCODER_KEY = "AwtDa3EncoderKey";
    private static final String DECODER_KEY = "AwtDa3DecoderKey";

    /** for encoding/decoding envelopes */
    private AwtDa3CodecService codec;

    public EnvelopeFilter(AwtDa3CodecService codec) {
        this.codec = codec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {

        // create the needed AWT-DA 3 decoder and encoder
        session.setAttribute(ENCODER_KEY, codec.createEnvelopeEncoder());
        session.setAttribute(DECODER_KEY, codec.createEnvelopeDecoder());
        nextFilter.sessionCreated(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message)
            throws Exception {
        if (message instanceof IoBuffer) {
            // accumulate that buffer
            final EnvelopeOutput decoderOutput = new EnvelopeOutput();

            ((EnvelopeDecoder) session.getAttribute(DECODER_KEY)).decodeAndAccumulate(((IoBuffer) message).buf(),
                    decoderOutput);
            final AwtDa3Envelope env = decoderOutput.getEnvelope();
            if (env != null) {
                // write the envelope to the next filter
                LOG.debug("decoded one envelope : {}", env);
                nextFilter.messageReceived(session, env);
            } else {
                LOG.debug("no envelope decoded, we need to accumulate more bytes");
            }
        } else {
            LOG.error("We should receive IoBuffer, not {}", message.getClass().getCanonicalName());
            nextFilter.messageReceived(session, message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest)
            throws Exception {
        if (writeRequest.getMessage() instanceof AwtDa3Envelope) {

            AwtDa3Envelope response = (AwtDa3Envelope) writeRequest.getMessage();

            LOG.debug("encoding response : {}", response);

            final ByteBuffer encodedBuffer = ((EnvelopeEncoder) session.getAttribute(ENCODER_KEY)).encode(response);

            nextFilter.filterWrite(
                    session,
                    new DefaultWriteRequest(IoBuffer.wrap(encodedBuffer), writeRequest.getFuture(), writeRequest
                            .getDestination()));
        } else {
            LOG.error("We should send AwtDa3Envelop, not {}", writeRequest.getMessage().getClass().getCanonicalName());
            nextFilter.filterWrite(session, writeRequest);
        }
    }

    private class EnvelopeOutput implements DecoderOutput<AwtDa3Envelope> {
        private AwtDa3Envelope envelope = null;

        /**
         * {@inheritDoc}
         */
        @Override
        public void decoded(final AwtDa3Envelope pdu) {
            envelope = pdu;
        }

        public AwtDa3Envelope getEnvelope() {
            return envelope;
        }
    }
}
