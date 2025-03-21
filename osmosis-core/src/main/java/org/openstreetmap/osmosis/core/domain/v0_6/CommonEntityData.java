// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.core.util.LazyHashMap;
import org.openstreetmap.osmosis.core.util.LongAsInt;


/**
 * Contains data common to all entity types. This is separated from the entity class to allow it to
 * be instantiated before all the data required for a full entity is available.
 */
public class CommonEntityData implements Storeable {
	
	private long id;
	private TagCollection tags;
	private Map<String, Object> metaTags;
	private boolean readOnly;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 */
	public CommonEntityData(long id) {
		// Chain to the more specific constructor
		this.id = id;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param tags
	 *            The tags to apply to the object.
	 */
	public CommonEntityData(
			long id, Collection<Tag> tags) {
		// Chain to the more specific constructor
		this.id = id;
		this.tags = new TagCollectionImpl(tags);
		metaTags = new LazyHashMap<String, Object>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public CommonEntityData(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			new TagCollectionImpl(sr, scr)
		);
		
		int metaTagCount;
		
		metaTagCount = sr.readInteger();
		metaTags = new LazyHashMap<String, Object>();
		for (int i = 0; i < metaTagCount; i++) {
			metaTags.put(sr.readString(), sr.readString());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(id);
		tags.store(sw, scr);
		sw.writeInteger(metaTags.size());
		for (Entry<String, Object> tag : metaTags.entrySet()) {
			sw.writeString(tag.getKey());
			sw.writeString(tag.getValue().toString());
		}
	}
	
	
	/**
	 * Compares the tags on this entity to the specified tags. The tag
	 * comparison is based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTags
	 *            The tags to compare to.
	 * @return 0 if equal, &lt; 0 if considered "smaller", and &gt; 0 if considered
	 *         "bigger".
	 */
	protected int compareTags(Collection<Tag> comparisonTags) {
		List<Tag> tags1;
		List<Tag> tags2;
		
		tags1 = new ArrayList<Tag>(tags);
		tags2 = new ArrayList<Tag>(comparisonTags);
		
		Collections.sort(tags1);
		Collections.sort(tags2);
		
		// The list with the most tags is considered bigger.
		if (tags1.size() != tags2.size()) {
			return tags1.size() - tags2.size();
		}
		
		// Check the individual tags.
		for (int i = 0; i < tags1.size(); i++) {
			int result = tags1.get(i).compareTo(tags2.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Gets the identifier.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}


	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            The identifier.
	 */
	public void setId(long id) {
		assertWriteable();
		
		this.id = id;
	}


	/**
	 * Returns the attached tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The tags.
	 */
	public Collection<Tag> getTags() {
		return tags;
	}


	/**
	 * Returns the attached meta tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The metaTags.
	 */
	public Map<String, Object> getMetaTags() {
		return metaTags;
	}


	/**
	 * Indicates if the object has been set to read-only. A read-only object
	 * must be cloned in order to make updates. This allows objects shared
	 * between multiple threads to be locked for thread safety.
	 * 
	 * @return True if the object is read-only.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}


	/**
	 * Ensures that the object is writeable. If not an exception will be thrown.
	 * This is intended to be called within all update methods.
	 */
	protected void assertWriteable() {
		if (readOnly) {
			throw new OsmosisRuntimeException(
					"The object has been marked as read-only.  It must be cloned to make changes.");
		}
	}


	/**
	 * Configures the object to be read-only. This should be called if the object is to be processed
	 * by multiple threads concurrently. It updates the read-only status of the object, and makes
	 * all collections unmodifiable. This must be overridden by sub-classes to make their own
	 * collections unmodifiable.
	 */
	public void makeReadOnly() {
		if (!readOnly) {
			tags = new UnmodifiableTagCollection(tags);
			metaTags = Collections.unmodifiableMap(metaTags);
			
			readOnly = true;
		}
	}


	/**
	 * Returns a writable instance of this object. If the object is read-only a clone is created,
	 * if it is already writable then this object is returned.
	 * 
	 * @return A writable instance of this object.
	 */
	public CommonEntityData getWriteableInstance() {
		if (isReadOnly()) {
			return new CommonEntityData(id, tags);
		} else {
			return this;
		}
	}
}
