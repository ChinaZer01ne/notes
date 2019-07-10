

package java.util;

import java.util.function.UnaryOperator;


public interface List<E> extends Collection<E> {

    //返回集合大小
    int size();

    //判断集合是否为空
    boolean isEmpty();

    //是否包含某元素
    boolean contains(Object o);

    Iterator<E> iterator();

    //将集合转换成数组（Object[]）
    Object[] toArray();

    <T> T[] toArray(T[] a);

    //添加元素
    boolean add(E e);

    //移除元素
    boolean remove(Object o);

    //是否包含另一个集合
    boolean containsAll(Collection<?> c);

    //将另一个集合添加到该集合
    boolean addAll(Collection<? extends E> c);

    boolean addAll(int index, Collection<? extends E> c);

    boolean removeAll(Collection<?> c);

    boolean retainAll(Collection<?> c);

    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }

    void clear();

    boolean equals(Object o);

    int hashCode();
    
    E get(int index);

    E set(int index, E element);

    void add(int index, E element);

    E remove(int index);

    int indexOf(Object o);

    int lastIndexOf(Object o);

    ListIterator<E> listIterator();

    ListIterator<E> listIterator(int index);

    List<E> subList(int fromIndex, int toIndex);

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
