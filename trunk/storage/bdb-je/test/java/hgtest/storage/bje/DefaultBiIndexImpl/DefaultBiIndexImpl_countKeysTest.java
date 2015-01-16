package hgtest.storage.bje.DefaultBiIndexImpl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseNotFoundException;
import org.easymock.EasyMock;
import org.hypergraphdb.HGException;
import org.hypergraphdb.storage.bje.DefaultBiIndexImpl;
import org.hypergraphdb.transaction.HGTransactionManager;
import org.powermock.api.easymock.PowerMock;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Yuriy Sechko
 */
public class DefaultBiIndexImpl_countKeysTest extends
		DefaultBiIndexImplTestBasis
{

	private DefaultBiIndexImpl<Integer, String> indexImpl;

	private void startupIndex()
	{
		mockStorage();
		PowerMock.replayAll();
		indexImpl = new DefaultBiIndexImpl<Integer, String>(INDEX_NAME,
				storage, transactionManager, keyConverter, valueConverter,
				comparator);
		indexImpl.open();
	}

	@Test
	public void useNullValue() throws Exception
	{
		final Exception expected = new NullPointerException();

		startupIndex();

		try
		{
			indexImpl.countKeys(null);
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
		}
		finally
		{
			indexImpl.close();
		}
	}

	@Test
	public void thereAreNotEntriesAdded() throws Exception
	{
		final long expected = 0;

		startupIndex();

		final long actual = indexImpl.countKeys("this value doesn't exist");

		assertEquals(actual, expected);
        indexImpl.close();
	}

    @Test
    public void thereAreSeveralEntriesByDesiredValueDoesNotExist() throws Exception {
        final long expected = 0;

        startupIndex();
        indexImpl.addEntry(1, "one");
        indexImpl.addEntry(2, "two");
        indexImpl.addEntry(3, "three");

        final long actual = indexImpl.countKeys("none");

        assertEquals(actual, expected);
        indexImpl.close();
    }

    @Test
    public void thereIsOnDesiredValue() throws Exception {
        final long expected = 1;

        startupIndex();
        indexImpl.addEntry(22, "twenty two");
        indexImpl.addEntry(33, "thirty three");

        final long actual = indexImpl.countKeys("twenty two");

        assertEquals(actual, expected);
        indexImpl.close();
    }

    @Test
    public void thereAreSeveralDesiredValues() throws Exception {
        final long expected = 2;

        startupIndex();
        indexImpl.addEntry(1, "one");
        indexImpl.addEntry(2, "two");
        indexImpl.addEntry(11, "one");

        final long actual = indexImpl.countKeys("one");

        assertEquals(actual, expected);
        indexImpl.close();
    }

    @Test
    public void indexIsNotOpened() throws Exception
    {
        final Exception expected = new NullPointerException();

        PowerMock.replayAll();
        indexImpl = new DefaultBiIndexImpl<Integer, String>(INDEX_NAME,
                storage, transactionManager, keyConverter, valueConverter, comparator);

        try
        {
            indexImpl.countKeys("some value");
        }
        catch (Exception occurred)
        {
            assertEquals(occurred.getClass(), expected.getClass());
        }
    }

    @Test
    public void transactionManagerThrowsException() throws Exception
    {
        final Exception expected = new HGException(
                "This exception is thrown by fake transaction manager.");

        mockStorage();
        final HGTransactionManager fakeTransactionManager = PowerMock
                .createStrictMock(HGTransactionManager.class);
        EasyMock.expect(fakeTransactionManager.getContext()).andThrow(
                new DatabaseNotFoundException("This exception is thrown by fake transaction manager."));
                PowerMock.replayAll();
        indexImpl = new DefaultBiIndexImpl<Integer, String>(INDEX_NAME,
                storage, transactionManager, keyConverter, valueConverter, comparator);
        indexImpl.open();
        indexImpl.addEntry(0, "red");

        // inject fake transaction manager
        final Field transactionManagerField = indexImpl.getClass()
                .getSuperclass().getDeclaredField("transactionManager");
        transactionManagerField.setAccessible(true);
        transactionManagerField.set(indexImpl, fakeTransactionManager);
        try
        {
            indexImpl.countKeys("yellow");
        }
        catch (Exception occurred)
        {
            assertEquals(occurred.getClass(), expected.getClass());
            assertTrue(occurred.getMessage().contains(expected.getMessage()));
        }
        finally
        {
            indexImpl.close();
        }
    }
}
