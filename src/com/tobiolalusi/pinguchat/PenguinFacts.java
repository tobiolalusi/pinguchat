package com.tobiolalusi.pinguchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PenguinFacts {
	List<String> penguinFacts;

	PenguinFacts() {
		this.penguinFacts = new ArrayList<>();
		penguinFacts.add("Penguins eat snow as a source of fresh water.");
		penguinFacts.add("The name comes from Welsh terms 'pen', meaning head and 'gwyn', meaning white.");
		penguinFacts.add("A penguin is an unofficial symbol of the United States Libertarian Party.");
		penguinFacts.add("Penguins lay eggs.");
		penguinFacts.add("In cold places, male penguins balance eggs on their feet and cover with belly flap to keep them warm.");
		penguinFacts.add("When the chick hatches, it immediately starts calling so that its parents will learn to recognize its voice.");
		penguinFacts.add("Penguins can’t fly, they swim.");
		penguinFacts.add("The Linux mascot ‘Tux’ is a penguin.");
		penguinFacts.add("Penguin chicks have fluffy feathers.");
		penguinFacts.add("A group of penguins are called colonies or rookery.");
		penguinFacts.add("They usually move in large groups to keep warm.");
		penguinFacts.add("Penguins can jump 6 feet out of water.");
		penguinFacts.add("Penguins live in the Southern Hemisphere.");
		penguinFacts.add("Most penguins can swim about 15 miles per hour.");
		penguinFacts.add("Penguins have insulating layers of air, skin, and blubber.");
		penguinFacts.add("They are ancient species that first appeared around 40 million years ago.");
		penguinFacts.add("There are 17 different species of penguins in the world, the most commonly recognized being the Emperor penguin.");
		penguinFacts.add("The first penguin fossil to be discovered was found in rocks that were around 25 million years old.");
		penguinFacts.add("A prehistoric skeleton of a penguin was found and is actually bigger than any living penguin that exists, it is believed they were up to 5ft tall (1.5 meters).");
		penguinFacts.add("The four penguins in the film Madagascar are named Skipper, Kowalski, Rico and Private.");
		penguinFacts.add("Penguins open their feather to feel the cold.");
		penguinFacts.add("Their white bellies blend with the snow and sunlight making it difficult for an underwater predator to see them.");
		penguinFacts.add("About 75% of a penguins life is spent in water, where they do all their hunting.");
		penguinFacts.add("Penguins use their wings for swimming.");
		penguinFacts.add("In general, a penguins lifespan ranges from 15 to 20 years.");
		penguinFacts.add("TUM Informatik students love penguins.");
		penguinFacts.add("TUM Informatik mascot should be a penguin named Boe.");
	}

	public List<String> getPenguinFacts() {
		return penguinFacts;
	}

	public static String getRandomPenguinFact() {
		List<String> penguinFacts = new PenguinFacts().getPenguinFacts();
		Random random = new Random();
		return penguinFacts.get(random.nextInt(penguinFacts.size()));
	}
}
