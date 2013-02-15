package m3da.server.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;

import m3da.server.store.StoreService;

import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sierrawireless.airvantage.tech.awtda3codec.AwtDa3CodecService;
import com.sierrawireless.airvantage.tech.awtda3codec.impl.AwtDa3CodecServiceImpl;

public class M3daTcpServer {

    static final Logger LOG = LoggerFactory.getLogger(M3daTcpServer.class);

    private final int idleTimeInSec;
    private final int port;
    private final int executorCoreSize;
    private final int executorMaxSize;

    private final AwtDa3CodecService codec = new AwtDa3CodecServiceImpl();

    private final NioSocketAcceptor acceptor;

    private final Handler handler;

    public M3daTcpServer(int processorCount, int idleTimeInSec, int port, int executorCoreSize, int executorMaxSize,
            StoreService store) {
        super();
        this.idleTimeInSec = idleTimeInSec;
        this.port = port;
        this.executorCoreSize = executorCoreSize;
        this.executorMaxSize = executorMaxSize;
        this.acceptor = new NioSocketAcceptor(processorCount);
        this.handler = new Handler(store, codec);
    }

    public void start() {

        acceptor.getSessionConfig().setBothIdleTime(idleTimeInSec);
        acceptor.getSessionConfig().setReuseAddress(true);
        acceptor.getSessionConfig().setTcpNoDelay(true);
        acceptor.setReuseAddress(true);

        // filter for dumping incoming/outgoing TCP data
        final LoggingFilter firstLogger = new LoggingFilter(this.getClass().getName());
        firstLogger.setMessageReceivedLogLevel(LogLevel.INFO);
        firstLogger.setSessionOpenedLogLevel(LogLevel.INFO);
        firstLogger.setSessionCreatedLogLevel(LogLevel.INFO);
        firstLogger.setSessionClosedLogLevel(LogLevel.INFO);
        firstLogger.setMessageSentLogLevel(LogLevel.INFO);

        // exception are already logged in the IoHandler, no need to duplicate the stacktrace
        firstLogger.setExceptionCaughtLogLevel(LogLevel.NONE);
        acceptor.getFilterChain().addFirst("LOGGER", firstLogger);

        // filter for encoding/decoding the AWTDA3 envelopes
        acceptor.getFilterChain().addLast("ENVCODEC", new EnvelopeFilter(codec));
        // thread pool for long lasting API calls after the decoding
        acceptor.getFilterChain().addLast("EXECUTOR", new ExecutorFilter(executorCoreSize, executorMaxSize));

        // plug the server logic
        acceptor.setHandler(handler);

        try {
            // bind the port
            LOG.info("bound port : {} for M3DA TCP connections", port);
            acceptor.bind(new InetSocketAddress(port));

        } catch (final IOException e) {
            throw new IllegalStateException("cannot bind the AWTDA3 server port (" + port + ")", e);
        }

    }

    public void stop() {
        acceptor.unbind();
        acceptor.dispose();
    }

}
