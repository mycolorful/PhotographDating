package per.yrj.photographdating.network;

import android.util.Log;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

import per.yrj.photographdating.request.Request2Server;
import per.yrj.photographdating.request.RequestBean;

/**
 * Created by YiRenjie on 2016/6/18.
 */
public class Connector {
    private String host;
    private int port;
    private boolean isConnected;
    private IoListener mIoListener;
    private ConnectListener mConnectListener;
    private NioSocketConnector mSocketConnector;
    private IoSession mIoSession;
    private int poolSize;

    private LinkedBlockingQueue<String> mRequestQueue = new LinkedBlockingQueue<>(8);

    public Connector(String host, int port, int poolSize) {
        this.host = host;
        this.port = port;
        this.poolSize = poolSize;
    }

    public void connect() {
        if (isConnected) {
            return;
        }

        int count = 0;
        while (count < 3) {

            try {
                if (count == 1) {
                    mConnectListener.onReconnected();
                }
                mSocketConnector = new NioSocketConnector();
                // 设置超时时间
                mSocketConnector.setConnectTimeoutMillis(30 * 1000);
                // 设置过滤链
                DefaultIoFilterChainBuilder filterChain = mSocketConnector
                        .getFilterChain();
                filterChain.addLast("codec", new ProtocolCodecFilter(
                        new TextLineCodecFactory(Charset.forName("utf-8"))));
                // 设置监听
                mSocketConnector.setHandler(new ConnectHandler());

                ConnectFuture future = mSocketConnector.connect(new InetSocketAddress(host, port));
                // 等待连接
                future.awaitUninterruptibly();

                mIoSession = future.getSession();
                isConnected = true;
                // 对外告知已经连接上
                if (mConnectListener != null) {
                    mConnectListener.onConnected();
                }

                // 开启多个线程同时处理请求队列中的request
                for (int i = 0; i < poolSize; i++) {
                    new Thread(new RequestWorker()).start();
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                isConnected = false;
                count++;
            }
        }

    }

    /**
     * 将request加入到请求队列
     * @param request
     */
    public void addRequest(RequestBean request){
        try {
            mRequestQueue.put(request.getTransport());
        } catch (InterruptedException e) {
            e.printStackTrace();
            mIoListener.onOutPutFailed(request.getTransport(), e);
        }
    }

    private class RequestWorker implements Runnable {

        @Override
        public void run() {
            while (isConnected) {
                    String requestString = null;
                try {
                    requestString = mRequestQueue.take();
                    mIoSession.write(requestString);
                } catch (InterruptedException e) {
                    if (mIoListener != null){
                        mIoListener.onOutPutFailed(requestString, e);
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnect() {
        try {
            if (mSocketConnector != null) {
                isConnected = false;
                mSocketConnector.dispose();
                mSocketConnector = null;
            }
        } catch (Exception e) {
            Log.e("error", "" + e.getMessage());
        }
    }


    public boolean isConnected() {
        return isConnected;
    }

    private class ConnectHandler implements IoHandler {

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            SocketSessionConfig cfg = (SocketSessionConfig) session.getConfig();
            cfg.setKeepAlive(true);
            cfg.setReadBufferSize(4 * 1024);
            cfg.setReceiveBufferSize(4 * 1024);
            cfg.setSendBufferSize(4 * 1024);
            cfg.setTcpNoDelay(true);
            cfg.setBothIdleTime(60 * 1000);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {

        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            isConnected = false;
            Log.d("connector", "sessionClosed");
            Log.d("connector", "sessionOpen : " + session.getId());

            if (mConnectListener != null) {
                mConnectListener.onDisconnected();
            }

        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            Log.d("receive", " " + message.toString().trim());

            if (mIoListener != null) {
                mIoListener.onInputComed(session, message);
            }
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {

        }
    }

    /**
     * 设置输入输出监听
     *
     * @param listener
     */
    public void setIOListener(IoListener listener) {
        this.mIoListener = listener;
    }

    /**
     * 设置连接监听
     *
     * @param listener
     */
    public void setConnectListener(ConnectListener listener) {
        this.mConnectListener = listener;
    }

    public interface IoListener {
        /**
         *
         * @param msg 发送的请求内容
         * @param e 异常
         */
        void onOutPutFailed(String msg, Exception e);

        void onInputComed(IoSession session, Object msg);
    }

    /**
     * 连接监听
     */
    public interface ConnectListener {
        void connecting();

        void onConnected();

        void onReconnected();

        void onDisconnected();
    }

}
