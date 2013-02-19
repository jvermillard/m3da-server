package m3da.server.tcp;

import static org.apache.commons.io.Charsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.store.Message;
import m3da.server.store.StoreService;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.sierrawireless.airvantage.tech.awtda3codec.AwtDa3CodecService;
import com.sierrawireless.airvantage.tech.awtda3codec.BysantDecoder;
import com.sierrawireless.airvantage.tech.awtda3codec.DecoderOutput;
import com.sierrawireless.airvantage.tech.awtda3codec.HeaderKey;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3DeltasVector;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3Envelope;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3Message;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3Pdu;
import com.sierrawireless.airvantage.tech.awtda3codec.dto.AwtDa3QuasiPeriodicVector;

public class Handler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);
    private StoreService store;

    private AwtDa3CodecService codec;

    public Handler(StoreService store, AwtDa3CodecService codec) {
        this.store = store;
        this.codec = codec;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.error("unexpected exception : ", cause);
        session.close(true);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {

        session.setAttribute("decoder", codec.createBodyDecoder());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        session.close(false);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof AwtDa3Envelope) {
            AwtDa3Envelope env = (AwtDa3Envelope) message;

            final String comId = new String(((ByteBuffer) env.getHeader().get(HeaderKey.ID)).array(), UTF_8);
            LOG.info("client communication identifier : {}", comId);

            if (env.getPayload().length > 0) {
                BysantDecoder decoder = (BysantDecoder) session.getAttribute("decoder");
                ListDecoder out = new ListDecoder();
                decoder.decodeAndAccumulate(ByteBuffer.wrap(env.getPayload()), out);
                List<Object> decoded = out.list;
                List<Message> data = new ArrayList<Message>(decoded.size());

                for (Object o : decoded) {
                    if (o instanceof AwtDa3Message) {
                        AwtDa3Message msg = (AwtDa3Message) o;

                        // uncompress list of values (quasicperiodic vector, etc..)
                        Map<String, List<?>> bodyData = new HashMap<String, List<?>>();
                        for (Map.Entry<Object, Object> e : msg.getBody().entrySet()) {
                            bodyData.put(e.getKey().toString(), extractList(e.getValue()));
                        }
                        data.add(new Message(msg.getPath(), bodyData));
                    }
                }
                store.enqueueReceivedData(comId, System.nanoTime(), data);
            }
        }
    }

    /**
     * Extract a list of value following the M3DA convention : extract QuasiPeriodic and Delta vectors. Convert non list
     * item to list with one element
     */
    private List<?> extractList(final Object v) {
        List<?> valueList;
        if (v instanceof List) {
            valueList = (List<?>) v;
        } else if (v instanceof AwtDa3DeltasVector) {
            valueList = ((AwtDa3DeltasVector) v).asFlatList();
        } else if (v instanceof AwtDa3QuasiPeriodicVector) {
            valueList = ((AwtDa3QuasiPeriodicVector) v).asFlatList();
        } else if (v instanceof ByteBuffer) {
            // as String (TODO : handle binary data)
            valueList = Collections.singletonList(new String(((ByteBuffer) v).array(), UTF_8));
        } else {
            valueList = Collections.singletonList(v);
        }
        return valueList;
    }

    /**
     * Decoder output accumulating the data in a list
     */
    private static class ListDecoder implements DecoderOutput<AwtDa3Pdu> {
        private final List<Object> list = Lists.newArrayList();

        /**
         * {@inheritDoc}
         */
        @Override
        public void decoded(final AwtDa3Pdu pdu) {
            list.add(pdu);
        }
    }
}