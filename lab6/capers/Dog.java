package capers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static capers.CapersRepository.CAPERS_FOLDER;
import static capers.Utils.*;

/** Represents a dog that can be serialized.
 * @author TODO
*/
public class Dog implements Serializable{ // TODO

    /** Folder that dogs live in. */
    static final File DOG_FOLDER = join(CAPERS_FOLDER,"/dogs");


    /** Age of dog. */
    private int age;
    /** Breed of dog. */
    private String breed;
    /** Name of dog. */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     * @param name Name of dog
     * @param breed Breed of dog
     * @param age Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.age = age;
        this.breed = breed;
        this.name = name;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        var dogFile = join(DOG_FOLDER, "/", name);
        if (!dogFile.exists()) {
            throw error("no such dog: %s", name);
        }
        var dog = readObject(dogFile, Dog.class);
        System.out.println("loaded: "+ dog.toString());
        return dog;
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        age += 1;
        System.out.println(toString());
        System.out.println("Happy birthday! Woof! Woof!");
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        if ( !DOG_FOLDER.exists() && !DOG_FOLDER.mkdir()) {
            throw error("failed to save %s", name);
        }

        var dogFile = join(DOG_FOLDER, "/", name);
        try {
            if (dogFile.exists()){
                dogFile.delete();
                dogFile.createNewFile();
            }
        } catch (IOException e) {
            throw error(e.getMessage());
        }
        writeObject(dogFile, this);
        System.out.println(toString());
    }

    @Override
    public String toString() {
        return String.format(
            "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
            name, breed, age);
    }

}
