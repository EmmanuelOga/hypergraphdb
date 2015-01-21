package hgtest.storage.bje.DefaultIndexImpl;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.StatsConfig;
import org.easymock.EasyMock;
import org.hypergraphdb.HGException;
import org.hypergraphdb.storage.bje.DefaultIndexImpl;
import org.powermock.api.easymock.PowerMock;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Yuriy Sechko
 */
public class DefaultIndexImpl_countTest extends DefaultIndexImplTestBasis
{
	@Test
	public void indexIsNotOpened() throws Exception
	{
		PowerMock.replayAll();
		final DefaultIndexImpl<Integer, String> index = new DefaultIndexImpl<Integer, String>(
				INDEX_NAME, storage, transactionManager, keyConverter,
				valueConverter, comparator);

		try
		{
			index.count();
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), NullPointerException.class);
		}

	}

	@Test
	public void thereAreNotAddedEntries() throws Exception
	{
		final long expected = 0;

		startupIndex();

		final long actual = index.count();

		assertEquals(actual, expected);
		index.close();
	}

	@Test
	public void thereIsOneAddedEntry() throws Exception
	{
		final long expected = 1;

		startupIndex();
		index.addEntry(0, "0");

		final long actual = index.count();

		assertEquals(actual, expected);
		index.close();
	}

	@Test
	public void thereAreTwoEntriesAdded() throws Exception
	{
		final long expected = 2;

		startupIndex();
		index.addEntry(0, "0");
		index.addEntry(1, "1");

		final long actual = index.count();

		assertEquals(actual, expected);
		index.close();
	}

	@Test
	public void thereAreSeveralEntriesAdded() throws Exception
	{
		final long expected = 3;

		startupIndex();
		index.addEntry(0, "0");
		index.addEntry(1, "1");
		index.addEntry(2, "2");

		final long actual = index.count();

		assertEquals(actual, expected);
		index.close();
	}

	@Test
	public void databaseThrowsException() throws Exception
	{
		System.out.println("databaseThrowsException test");
		final Exception expected = new HGException(
				"This exception is thrown by fake database.");

		// create index and open it in usual way
		mockStorage();
		PowerMock.replayAll();
		final DefaultIndexImpl<Integer, String> index = new DefaultIndexImpl<Integer, String>(
				INDEX_NAME, storage, transactionManager, keyConverter,
				valueConverter, comparator);
		index.open();
		PowerMock.verifyAll();
		PowerMock.resetAll();

		// after index is opened inject fake database field and call
		// count() method
		final Field databaseField = index.getClass().getDeclaredField(
				DATABASE_FIELD_NAME);
		databaseField.setAccessible(true);
		// close real database before use fake
		final Database realDatabase = (Database) databaseField.get(index);
		realDatabase.close();
		// create fake database instance and imitate throwing exception
		final Database fakeDatabase = PowerMock
				.createStrictMock(Database.class);
		EasyMock.expect(
				fakeDatabase.getStats(EasyMock.<StatsConfig> anyObject()))
				.andThrow(
						new DatabaseNotFoundException(
								"This exception is thrown by fake database."));
		PowerMock.replayAll();
		// inject fake database into appropriate field
		databaseField.set(index, fakeDatabase);

		try
		{
			index.count();
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
			assertTrue(occurred.getMessage().contains(expected.getMessage()));
		}
		// set null value to DefaultIndexImpl.db field
		// without this setting Database.close() method on fake database instance is invoked somewhere
		// and all next tests fail because Powermock expects 'close' call
		databaseField.set(index, null);
	}
}
