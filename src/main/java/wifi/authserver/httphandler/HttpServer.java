/**
 * @author weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.httphandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import wifi.authserver.comm.LogHelper;

public class HttpServer {
	private int port;

	public HttpServer(int port) {
		this.port = port;
	}

	public void run() {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", new HttpHandler());
				return pipeline;
			}
		});

		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", false);

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));
	}

	public void start() {
		LogHelper.info("server start with port "+port+" \n");
		run();
	}
}
