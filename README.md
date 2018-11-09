# ByMock

This library is inspired by some work I did on a recent project at work.
It was popular with my team, so I decided to rewrite it from scratch and open source it.

I'd recommend reading this page first, but here's the line for your build.gradle:

```
compile 'io.poyarzun:bymock:1.0.1'
```

## Background

When writing automated tests - hopefully through TDD, you often need test doubles.
I'm a big fan of manually writing test doubles instead of relying on a mocking framework dsl.
Unfortunately, manually writing test doubles can get really tedious, especially in statically typed languages.
Kotlin has some features that make it easy, and this library adds a tiny helper method to cover one edge case that's still hard.

The core idea here is to use kotlin's object expression syntax to create highly cohesive tests.


See also: 

* [The Little Mocker](https://blog.cleancoder.com/uncle-bob/2014/05/14/TheLittleMocker.html)
* [The Test Double Rule of Thumb](http://engineering.pivotal.io/post/the-test-double-rule-of-thumb)
* [Kotlin object expressions](https://kotlinlang.org/docs/reference/object-declarations.html#object-expressions)
* [Kotlin delegation](https://kotlinlang.org/docs/reference/delegation.html)

## Usage

There are several kinds of test doubles described [here](https://blog.cleancoder.com/uncle-bob/2014/05/14/TheLittleMocker.html).
We'll be using a couple common interface definitions for each of these.

```kotlin
data class Person(val name: String, val age: Int)

interface PersonReader {
    fun findPeople(age: Int): List<Person>
}

interface PersonWriter {
    fun writePerson(person: Person)
}

interface PersonRepository: PersonReader, PersonWriter
```


Here's how I would write each:


### Dummies

> Dummy (a double that blows up when used)

```kotlin
val dummyPersonWriter = object : PersonWriter {
    override fun writePerson(person: Person) {
        throw IllegalStateException()
    }
}

// pass the double to a collaborator and exercise its interaction
```

### Stubs

> Stub (a double with hard-coded return values)

```kotlin
val stubPersonReader = object : PersonReader {
    override fun findPeople(age: Int): List<Person> {
        return listOf(Person(name = "John", age = 35))
    }
}

// pass the double to a collaborator and exercise its interaction
```

### Spies

> Spy (a double that you can interrogate to verify it was used correctly)

```kotlin
val spyPersonWriter = object : PersonReader {

    val writtenPeople = ArrayList<Person>()

    override fun writePerson(person: Person) {
        writtenPeople.add(person)
    }
}

// pass the double to a collaborator and exercise its interaction

// assert something about spyPersonWriter.writtenPeople
```

### Mocks

> Mock (a spy that verifies itself)

```kotlin
val mockPersonWriter = object : PersonReader {

    private val writtenPeople = ArrayList<Person>()

    override fun writePerson(person: Person) {
        writtenPeople.add(person)
    }
    
    fun verify() {
        assertTrue(writtenPeople.size == 1)
    }
}

// pass the double to a collaborator and exercise its interaction

mockPersonWriter.verify()
```

### Fakes

> Fake (a behavioral mimic)

```kotlin
val fakePersonRepository = object : PersonRepository {
    val writtenPeople = ArrayList<Person>()
    
    override fun findPeople(age: Int): List<Person> {
        return writtenPeople.filter {it.age == age}.toList()
    }
    
    override fun writePerson(person: Person) {
        writtenPeople.add(person)
    }
}

// pass the double to a collaborator and exercise its interaction
```


### Wait, where's the library?

Exactly. So far none of those examples use this library. In general, you don't need it.
On a recent kotlin project, we worked just fine without any mocking tools for quite a while.
The one issue we finally had was with *external*, *huge* interfaces. For example, Spring has an interface
called JdbcOperations which has **58** methods. In order to manually write an object-expression style double for that,
you'd have to manually override all 58 methods. That's clearly no fun. Instead, we wrote a tiny function called `dummy`.

Here's an example:

```kotlin
val stubbedResult = 0

val stubJdbcOperations = object : JdbcOperations by dummy() {
    override fun <T> queryForObject(sql: String, requiredType: Class<T>): T {
        return someResult as T
    }
}

// pass the double to a collaborator and exercise its interaction
```

In the example above, any call to methods which are not overridden will throw an IllegalStateException.
By using [kotlin delegation](https://kotlinlang.org/docs/reference/delegation.html), we can automatically
delegate all the methods we don't care to override to a dynamic proxy created in the dummy function. 

Go look at the source. It's one tiny file. Don't want to add a dependency for one file? 
I don't blame you. Just copy the one file into your project, I don't mind.
