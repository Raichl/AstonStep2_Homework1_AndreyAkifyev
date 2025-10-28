import java.util.Arrays;

public class MyHashMap<K, V> {

    /// Класс ноды используемой для хранения данных
    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> nextNode;
        final int hash;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.nextNode = next;
            this.hash = hash;
        }
    }

    /// Параметры размера и расширения при заполнении
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAX_CAPACITY = 1 << 30; //максимальный размер массива в Java 2^30

    /// Основные элементы необходимые для работы
    private Node<K, V>[] table; //сам массив хранящий ноды
    private int size; //текущий размер
    private final float loadFactor; //
    private int threshold;


    /// Основной конструктор
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Значение начального размера не может быть 0");
        }
        if (loadFactor <= 0 || loadFactor > 1 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Значение прцента заполненности не может быть 0% или меньше или больше 100%");
        }

        this.loadFactor = loadFactor;
        this.table = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) (initialCapacity * loadFactor);
    }

    /// Перегрузки для простоты использования со значениями по умолчанию
    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /// Вычисление хэша и индекса для распределения элементов в таблице

    private int hash(K key) {
        if (key == null) return 0;
        return key.hashCode();
    }

    private int indexFor(int hash, int length) {
        return Math.abs(hash % length);
    }

    /// Проверка равенства ключей

    private boolean keysEquals(K key1, K key2, int hash1, int hash2) {
        if (key1 == key2) return true;
        if (hash1 != hash2) return false;
        return key1 != null && key1.equals(key2);
    }

    /// Резайзинг, при заполнении на процент loadFactor нужно автоматически расширять внутреннюю таблицу и перезаписывать ее
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int oldCapacity;
        int newCapacity;

        if (oldTable == null) oldCapacity = 0;
        else oldCapacity = oldTable.length;

        if (oldCapacity == 0) {
            newCapacity = DEFAULT_INITIAL_CAPACITY;
        } else {
            newCapacity = oldCapacity * 2;
        }

        if (newCapacity > MAX_CAPACITY) {
            newCapacity = MAX_CAPACITY;
        }

        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * loadFactor);

        if (oldTable != null) {
            rehashElements(oldTable, newTable, oldCapacity, newCapacity);
        }

        table = newTable;
    }

    /// Перевод элементов из старой таблицы в новую при изменении размера
    private void rehashElements(Node<K, V>[] oldTable, Node<K, V>[] newTable, int oldCapacity, int newCapacity) {
        for (int i = 0; i < oldCapacity; i++) {
            Node<K, V> currentNode = oldTable[i];
            while (currentNode != null) {
                Node<K, V> nextNode = currentNode.nextNode;
                int newIndex = indexFor(currentNode.hash, newCapacity);
                currentNode.nextNode = newTable[newIndex].nextNode;
                newTable[newIndex] = currentNode;
                currentNode = nextNode;
            }
        }
    }

    /// Методы работы с HashMap

    ///  Получить размер
    public int size() {
        return size;
    }

    ///  Провериить на пустоту
    public boolean isEmpty() {
        return size == 0;
    }

    ///  Очистить HashMap
    public void clear() {
        if (table != null && size > 0) {
            Arrays.fill(table, null);
            size = 0;
        }
    }

    /// Проверить содержит ли значение
    public boolean containValue(V value) {
        if (table != null && size > 0) {
            for (Node<K, V> node : table) {
                while (node != null) {
                    if (value == null) {
                        if (node.value == null) return true;
                    } else {
                        if (value.equals(node.value)) return true;
                    }
                    node = node.nextNode;
                }
            }
        }
        return false;
    }

    /// Добавить значение в hashMap
    public V put(K key, V value) {
        return putVal(hash(key), key, value);
    }


    private V putVal(int hash, K key, V value) {
        if (table == null || table.length == 0) {
            resize();
        }
        int index = indexFor(hash, table.length);
        Node<K, V> current = table[index];
        while (current != null) {
            if (keysEquals(current.key, key, current.hash, hash)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.nextNode;
        }
        addNode(hash, key, value, index);
        return null;
    }

    private void addNode(int hash, K key, V value, int index) {
        Node<K, V> first = table[index];
        table[index] = new Node<>(hash, key, value, first);
        if (++size > threshold) {
            resize();
        }
    }

    /// Получить значение из HashMap
    public V get(K key){
        Node<K,V> node = getNode(key);
        return node == null ? null : node.value;
    }

    private Node<K,V> getNode(K key) {
        if (table == null || size == 0) return null;

        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Node<K,V> current = table[index];

        while (current != null){
            if (keysEquals(current.key, key,current.hash,hash)){
                return current;
            }
            current = current.nextNode;
        }
        return null;
    }
    ///  Проверка есть ли нода с заданным ключом
    public boolean containsKey(K key){
        return getNode(key) != null;
    }

    ///удаление эллемента
    public V remove(K key){
        Node<K,V> node = removeNode(key);
        return node == null ? null : node.value;
    }

    private Node<K,V> removeNode(K key) {
        if (table == null || size == 0) return null;

        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Node<K,V> current = table[index];
        Node<K,V> prev = null;

        while (current!=null){
            if(keysEquals(current.key, key,current.hash,hash)){
               if (prev == null){
                   table[index] = current.nextNode;
               }else{
                   prev.nextNode = current.nextNode;
               }
               size--;
               return current;
            }

            prev = current;
            current = current.nextNode;
        }
        return null;
    }
/// Пример использования
    public static void main(String[] args) {
        MyHashMap<String,Integer> map = new MyHashMap<>();
        /// Метод Put
        map.put("one",1);
        map.put("two",2);
        map.put("three",3);
        System.out.printf("Размер HashMap %d \n",map.size);
        /// Метод Get
        System.out.printf("Эллемент с ключом %s -> %d \n","one",map.get("one"));
        System.out.printf("Эллемент с ключом %s -> %d \n","two",map.get("two"));
        System.out.printf("Эллемент с ключом %s -> %d \n","three",map.get("three"));
        System.out.printf("Эллемент с ключом %s -> %d \n","four",map.get("four"));

        /// Метод Remove

        System.out.println("\nУдаляем элемент с ключом two");
        map.remove("two");
        System.out.printf("Размер HashMap %d \n",map.size);
        System.out.printf("Эллемент с ключом %s -> %d \n","one",map.get("one"));
        System.out.printf("Эллемент с ключом %s -> %d \n","two",map.get("two"));
        System.out.printf("Эллемент с ключом %s -> %d \n","three",map.get("three"));
        System.out.printf("Эллемент с ключом %s -> %d \n","four",map.get("four"));
    }

}