package solidstack.nio;

import java.nio.channels.SelectionKey;

public interface SocketChannelHandlerFactory
{
	SocketChannelHandler createHandler( Dispatcher dispatcher, SelectionKey key );
}
