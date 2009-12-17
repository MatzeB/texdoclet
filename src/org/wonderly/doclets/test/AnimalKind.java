package org.wonderly.doclets.test;

/**
 * Classification for animals (based on wikipedia Animal article)
 * @author matze
 */
public enum AnimalKind {
	/** the animal group porifera */
	PORIFERA("also called sponges"),
	/** the most common group deuterostomes */
	DEUTEROSTOMES("includes fish, amphibians, reptiles, birds and mammals"),
	/** the group of ecdysozoa */
	ECDYSOZOA("no description"),
	/** a group of animals I have no idea about */
	PLATYZOA("no description"),
	/** another group I have no clue about */
	LOPHOTROCHOZOA("no description");
	
	private String description;
	
	private AnimalKind(String description) {
		this.description = description;
	}
	
	/**
	 * Return a short description of the animal kind.
	 * @return description String
	 */
	public String getDescription() {
		return description;
	}
}
