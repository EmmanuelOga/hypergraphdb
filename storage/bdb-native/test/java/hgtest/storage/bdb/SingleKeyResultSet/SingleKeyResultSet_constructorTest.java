package hgtest.storage.bdb.SingleKeyResultSet;

import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import hgtest.storage.bdb.ResultSetTestBasis;
import org.easymock.EasyMock;
import org.hypergraphdb.HGException;
import org.hypergraphdb.storage.ByteArrayConverter;
import org.hypergraphdb.storage.bdb.BDBTxCursor;
import org.hypergraphdb.storage.bdb.SingleKeyResultSet;
import org.powermock.api.easymock.PowerMock;
import org.testng.annotations.Test;

import static hgtest.TestUtils.assertExceptions;

/**
 * @author Yuriy Sechko
 */
public class SingleKeyResultSet_constructorTest extends ResultSetTestBasis
{
	private final ByteArrayConverter<Integer> converter = new hgtest.TestUtils.ByteArrayConverterForInteger();
	private final DatabaseEntry key = new DatabaseEntry(
			new byte[] { 0, 0, 0, 0 });

	@Test
	public void bjeCursorIsNull() throws Exception
	{
		final Exception expected = new HGException(
				"java.lang.NullPointerException");

		try
		{
			new SingleKeyResultSet(null, key, converter);
		}
		catch (Exception occurred)
		{
			assertExceptions(occurred, expected);
		}
	}

	@Test
	public void keyIsNull() throws Exception
	{
		final Cursor realCursor = database.openCursor(
				transactionForTheEnvironment, null);
		// initialize cursor
		realCursor.put(new DatabaseEntry(new byte[] { 1, 2, 3, 4 }),
				new DatabaseEntry(new byte[] { 1, 2, 3, 4 }));
		final BDBTxCursor fakeCursor = PowerMock.createMock(BDBTxCursor.class);
		EasyMock.expect(fakeCursor.cursor()).andReturn(realCursor).times(2);
		PowerMock.replayAll();

		new SingleKeyResultSet(fakeCursor, null, converter);

		realCursor.close();
	}

	@Test
	public void converterIsNull() throws Exception
	{
		final Exception expected = new HGException(
				"java.lang.NullPointerException");

		final Cursor realCursor = database.openCursor(
				transactionForTheEnvironment, null);
		realCursor.put(new DatabaseEntry(new byte[] { 1, 2, 3, 4 }),
				new DatabaseEntry(new byte[] { 1, 2, 3, 4 }));
		final BDBTxCursor fakeCursor = PowerMock.createMock(BDBTxCursor.class);
		EasyMock.expect(fakeCursor.cursor()).andReturn(realCursor);
		PowerMock.replayAll();

		try
		{
			new SingleKeyResultSet(fakeCursor, key, null);
		}
		catch (Exception occurred)
		{
			assertExceptions(occurred, expected);
		}
		finally
		{
			realCursor.close();
		}
	}

	@Test
	public void fakeCursorThrowsException() throws Exception
	{
		final Exception expected = new HGException(
				"java.lang.IllegalStateException: This exception is thrown by fake cursor.");

		final Cursor realCursor = database.openCursor(
				transactionForTheEnvironment, null);
		realCursor.put(new DatabaseEntry(new byte[] { 1, 2, 3, 4 }),
				new DatabaseEntry(new byte[] { 1, 2, 3, 4 }));
		final BDBTxCursor fakeCursor = PowerMock.createMock(BDBTxCursor.class);
		// at the first call return real cursor
		EasyMock.expect(fakeCursor.cursor()).andReturn(realCursor);
		// at the second call throw exception
		EasyMock.expect(fakeCursor.cursor()).andThrow(
				new IllegalStateException(
						"This exception is thrown by fake cursor."));
		PowerMock.replayAll();

		try
		{
			new SingleKeyResultSet(fakeCursor, key, converter);
		}
		catch (Exception occurred)
		{
			assertExceptions(occurred, expected);
		}
		finally
		{
			realCursor.close();
		}
	}

	@Test
	public void allIsOk() throws Exception
	{
		final Cursor realCursor = database.openCursor(
				transactionForTheEnvironment, null);
		realCursor.put(new DatabaseEntry(new byte[] { 1, 2, 3, 4 }),
				new DatabaseEntry(new byte[] { 1, 2, 3, 4 }));
		final BDBTxCursor fakeCursor = PowerMock.createMock(BDBTxCursor.class);
		EasyMock.expect(fakeCursor.cursor()).andReturn(realCursor).times(2);
		PowerMock.replayAll();

		new SingleKeyResultSet<Integer>(fakeCursor, key, converter);

		realCursor.close();
	}
}
