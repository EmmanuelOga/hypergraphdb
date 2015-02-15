package hgtest.storage.bje;

import com.sleepycat.je.*;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.storage.BAUtils;
import org.hypergraphdb.storage.BAtoString;
import org.hypergraphdb.storage.ByteArrayConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * @author Yuriy Sechko
 */
public class TestUtils
{
	/**
	 * Deletes directory's content and then deletes directory itself. Deleting
	 * is not recursive.
	 * 
	 * @param directory
	 */
	public static void deleteDirectory(final File directory)
	{
		final File[] filesInTestDir = directory.listFiles();
		if (filesInTestDir != null)
		{
			for (final File eachFile : filesInTestDir)
			{
				eachFile.delete();
			}
		}
		directory.delete();
	}

	/**
	 * Iterates through result and copies encountered items to the list.
	 */
	public static <T> List<T> list(final HGSearchResult<T> result)
	{
		final List<T> outputList = new ArrayList<T>();
		while (result.hasNext())
		{
			final T currentValue = result.next();
			outputList.add(currentValue);
		}
		return outputList;
	}

	/**
	 * Puts all handles which are accessible from given result set into hash
	 * set. In some test cases stored data returned as
	 * {@link HGRandomAccessResult}. Two results cannot be compared directly. So
	 * we put all handles into set and that compare two sets. The order of
	 * handles in result set (obtained from database) is difficult to predict.
	 */
	public static Set<HGPersistentHandle> set(
			final HGRandomAccessResult<HGPersistentHandle> handles)
	{
		final Set<HGPersistentHandle> allHandles = new HashSet<HGPersistentHandle>();
		while (handles.hasNext())
		{
			allHandles.add(handles.next());
		}
		return allHandles;
	}

	/**
	 * Creates temporary file with given prefix and suffix.
	 * 
	 * @return link to the created file instance
	 */
	public static File createTempFile(final String prefix, final String suffix)
	{
		File tempFile;
		try
		{
			tempFile = File.createTempFile(prefix, suffix);
		}
		catch (IOException ioException)
		{
			throw new IllegalStateException(ioException);
		}
		return tempFile;
	}

	/**
	 * Shortcut for the {@link java.io.File#getCanonicalPath()}. But throws
	 * {@link java.lang.IllegalStateException } if something went wrong.
	 * 
	 * @return
	 */
	public static String getCanonicalPath(final File file)
	{
		String canonicalPath;
		try
		{
			canonicalPath = file.getCanonicalPath();
		}
		catch (IOException ioException)
		{
			throw new IllegalStateException(ioException);
		}
		return canonicalPath;
	}

	/**
	 * Converts from Integer number to appropriate byte array (in terms of
	 * HyperGraphDB)
	 */
	public static class ByteArrayConverterForInteger implements
			ByteArrayConverter<Integer>
	{
		public byte[] toByteArray(final Integer input)
		{
			final byte[] buffer = new byte[4];
			BAUtils.writeInt(input, buffer, 0);
			return buffer;
		}

		public Integer fromByteArray(final byte[] byteArray, final int offset,
				final int length)
		{
			return BAUtils.readInt(byteArray, 0);
		}
	}

	/**
	 * Converts from String object number to appropriate byte array (in terms of
	 * HyperGraphDB)
	 */
	public static class ByteArrayConverterForString implements
			ByteArrayConverter<String>
	{
		public byte[] toByteArray(final String input)
		{
			return BAtoString.getInstance().toByteArray(input);
		}

		public String fromByteArray(final byte[] byteArray, final int offset,
				final int length)
		{
			return BAtoString.getInstance().fromByteArray(byteArray, offset,
					length);
		}
	}

	// TODO: investigate how to compare messages but don't take Sleepycat's
	// TODO: library version into account
	/**
	 * Compares two instances which represent exceptions by:
	 * <ul>
	 * <li>by object's class</li>
	 * <li>by message</li>
	 * </ul>
	 * 
	 * TestNG assertion are in use.
	 */
	public static void assertExceptions(final Exception occurred,
			final Exception expected)
	{
		assertEquals(occurred.getClass(), expected.getClass());
		assertEquals(occurred.getMessage(), expected.getMessage());
	}

	/**
	 * Utility method. Puts given data as Integer-Integer pair to database. The
	 * separate transaction is performed.
	 */
	public static void putKeyValuePair(final Environment environment,
			final Database database, final Integer key, final Integer value)
	{
		final Transaction transactionForAddingTestData = environment
				.beginTransaction(null, null);
		database.put(
				transactionForAddingTestData,
				new DatabaseEntry(new TestUtils.ByteArrayConverterForInteger()
						.toByteArray(key)),
				new DatabaseEntry(new TestUtils.ByteArrayConverterForInteger()
						.toByteArray(value)));
		transactionForAddingTestData.commit();
	}

	/**
	 * Utility method. Puts given data as Integer-String pair to cursor.
	 */
	public static void putKeyValuePair(Cursor realCursor, final Integer key,
			final String value)
	{
		realCursor.put(
				new DatabaseEntry(new TestUtils.ByteArrayConverterForInteger()
						.toByteArray(key)),
				new DatabaseEntry(new TestUtils.ByteArrayConverterForString()
						.toByteArray(value)));
	}
}
