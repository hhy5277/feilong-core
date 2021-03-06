/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feilong.core.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.collections4.comparators.ReverseComparator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feilong.core.bean.ConvertUtil;
import com.feilong.core.bean.PropertyUtil;

/**
 * 属性比较器,自动获取 <code>T</code>中的属性名字是 {@link #propertyName}的值,进行比较,不用每个需要排序的字段创建 {@link Comparator}类.
 * 
 * <h3>关于 {@link #propertyName}:</h3>
 * 
 * <blockquote>
 * <p>
 * {@link #propertyName} 取出来的值,必须实现 {@link Comparable}接口,比如 {@link Integer}, {@link String}等类型
 * </p>
 * </blockquote>
 * 
 * <h3>顺序:</h3>
 * 
 * <blockquote>
 * <p>
 * 该类默认是<span style="color:red">正序</span>的形式,如果需要反序,请再使用 {@link ReverseComparator}进行包装
 * </p>
 * </blockquote>
 *
 * @author <a href="http://feitianbenyue.iteye.com/">feilong</a>
 * @param <T>
 *            the generic type
 * @see org.apache.commons.collections4.ComparatorUtils
 * @see "org.springframework.beans.support.PropertyComparator"
 * @see org.apache.commons.beanutils.BeanComparator
 * @see org.apache.commons.collections4.comparators.BooleanComparator
 * @see org.apache.commons.collections4.comparators.ReverseComparator
 * @see org.apache.commons.collections4.comparators.ComparableComparator
 * @see <a href=
 *      "http://stackoverflow.com/questions/19325256/java-lang-illegalargumentexception-comparison-method-violates-its-general-contr">java-
 *      lang-illegalargumentexception-comparison-method-violates-its-general-contr</a>
 * @see <a href=
 *      "http://stackoverflow.com/questions/11441666/java-error-comparison-method-violates-its-general-contract">Java error: Comparison
 *      method violates its general contract</a>
 * @since 1.2.0
 */
public class PropertyComparator<T> implements Comparator<T>,Serializable{

    /** The Constant serialVersionUID. */
    private static final long           serialVersionUID = -3159374167882773300L;

    /** The Constant LOGGER. */
    private static final Logger         LOGGER           = LoggerFactory.getLogger(PropertyComparator.class);

    /**
     * 指定bean对象排序属性名字(默认正序).
     * 
     * <p>
     * 泛型T对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     * <a href="../../bean/BeanUtil.html#propertyName">propertyName</a>,<br>
     * 该属性的value 必须实现 {@link Comparable}接口.
     * </p>
     */
    private final String                propertyName;

    /** The comparator. */
    @SuppressWarnings("rawtypes")
    private Comparator                  comparator;

    /** 反射提取出来的值,需要类型转成到的类型. */
    @SuppressWarnings("rawtypes")
    private Class<? extends Comparable> propertyValueConvertToClass;

    /**
     * The Constructor.
     * 
     * <h3>示例:</h3>
     * 
     * <blockquote>
     * 
     * <pre class="code">
     * List{@code <User>} list = new ArrayList{@code <>}();
     * list.add(new User(12L, 18));
     * list.add(new User(2L, 36));
     * list.add(new User(5L, 22));
     * list.add(new User(1L, 8));
     * 
     * Collections.sort(list, new PropertyComparator{@code <User>}("id"));
     * LOGGER.debug(JsonUtil.format(list));
     * </pre>
     * 
     * <b>返回:</b>
     * 
     * <pre class="code">
        [{
                "id": 1,
                "age": 8
            },
                    {
                "id": 2,
                "age": 36
            },
                    {
                "id": 5,
                "age": 22
            },
                    {
                "id": 12,
                "age": 18
        }]
     * </pre>
     * 
     * </blockquote>
     *
     * @param propertyName
     *            指定bean对象排序属性名字(默认正序).
     * 
     *            <p>
     *            泛型T对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *            <a href="../../bean/BeanUtil.html#propertyName">propertyName</a>,<br>
     *            该属性的value 必须实现 {@link Comparable}接口.
     *            </p>
     */
    public PropertyComparator(String propertyName){
        Validate.notBlank(propertyName, "propertyName can't be blank!");
        this.propertyName = propertyName;
        LOGGER.trace("propertyName:[{}]", propertyName);
    }

    /**
     * The Constructor.
     *
     * @param propertyName
     *            指定bean对象排序属性名字(默认正序).
     * 
     *            <p>
     *            泛型T对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *            <a href="../../bean/BeanUtil.html#propertyName">propertyName</a>,<br>
     *            该属性的value 必须实现 {@link Comparable}接口.
     *            </p>
     * @param comparator
     *            the comparator
     * @since 1.5.4
     */
    @SuppressWarnings("rawtypes")
    public PropertyComparator(String propertyName, Comparator comparator){
        Validate.notBlank(propertyName, "propertyName can't be blank!");
        this.propertyName = propertyName;
        this.comparator = comparator;
        LOGGER.trace("propertyName:[{}]", propertyName);
    }

    /**
     * The Constructor.
     * 
     * <h3>使用场景:</h3>
     * <blockquote>
     * 诸如需要排序的对象指定属性类型是数字,但是申明类型的时候由于种种原因是字符串,<br>
     * 此时需要排序,如果不转成Integer比较的话,字符串比较13 将会在 3数字的前面
     * </blockquote>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <p>
     * 我们现在有这样的数据,其中属性 totalNo是字符串类型的
     * </p>
     * 
     * <pre class="code">
     * CourseEntity courseEntity1 = new CourseEntity();
     * courseEntity1.setTotalNo("3");
     * 
     * CourseEntity courseEntity2 = new CourseEntity();
     * courseEntity2.setTotalNo("13");
     * 
     * List{@code <CourseEntity>} courseList = new ArrayList{@code <>}();
     * courseList.add(courseEntity1);
     * courseList.add(courseEntity2);
     * </pre>
     * 
     * 如果 我们只是使用 propertyName进行排序的话:
     * 
     * <pre class="code">
     * Collections.sort(courseList, new PropertyComparator{@code <CourseEntity>}("totalNo"));
     * LOGGER.debug(JsonUtil.format(courseList));
     * </pre>
     * 
     * 那么<b>返回:</b>
     * 
     * <pre class="code">
     * [{
     * "totalNo": "13",
     * "name": ""
     * },{
     * "totalNo": "3",
     * "name": ""
     * }]
     * </pre>
     * 
     * 如果我们使用 propertyName+ propertyValueConvertToClass进行排序的话:
     * 
     * <pre class="code">
     * Collections.sort(courseList, new PropertyComparator{@code <CourseEntity>}("totalNo", Integer.class));
     * LOGGER.debug(JsonUtil.format(courseList));
     * </pre>
     * 
     * <b>返回:</b>
     * 
     * <pre class="code">
     * [{
     * "totalNo": "3",
     * "name": ""
     * },{
     * "totalNo": "13",
     * "name": ""
     * }]
     * </pre>
     * 
     * </blockquote>
     *
     * @param propertyName
     *            指定bean对象排序属性名字(默认正序).
     * 
     *            <p>
     *            泛型T对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *            <a href="../../bean/BeanUtil.html#propertyName">propertyName</a>,<br>
     *            该属性的value 必须实现 {@link Comparable}接口.
     *            </p>
     * @param propertyValueConvertToClass
     *            反射提取出来的值,需要类型转成到的类型
     * @since 1.5.0
     */
    @SuppressWarnings("rawtypes")
    public PropertyComparator(String propertyName, Class<? extends Comparable> propertyValueConvertToClass){
        Validate.notBlank(propertyName, "propertyName can't be blank!");
        this.propertyName = propertyName;
        this.propertyValueConvertToClass = propertyValueConvertToClass;
        LOGGER.trace("propertyName:[{}]", propertyName);
    }

    /**
     * The Constructor.
     * *
     * <h3>使用场景:</h3>
     * <blockquote>
     * 诸如需要排序的对象指定属性类型是数字,但是申明类型的时候由于种种原因是字符串,<br>
     * 此时需要排序,如果不转成Integer比较的话,字符串比较13 将会在 3数字的前面
     * </blockquote>
     *
     * @param propertyName
     *            指定bean对象排序属性名字(默认正序).
     * 
     *            <p>
     *            泛型T对象指定的属性名称,Possibly indexed and/or nested name of the property to be modified,参见
     *            <a href="../../bean/BeanUtil.html#propertyName">propertyName</a>,<br>
     *            该属性的value 必须实现 {@link Comparable}接口.
     *            </p>
     * @param propertyValueConvertToClass
     *            反射提取出来的值,需要类型转成到的类型
     * @param comparator
     *            the comparator
     * @since 1.5.4
     */
    @SuppressWarnings("rawtypes")
    public PropertyComparator(String propertyName, Class<? extends Comparable> propertyValueConvertToClass, Comparator comparator){
        Validate.notBlank(propertyName, "propertyName can't be blank!");
        this.propertyName = propertyName;
        this.propertyValueConvertToClass = propertyValueConvertToClass;
        this.comparator = comparator;
        LOGGER.trace("propertyName:[{}]", propertyName);
    }

    /**
     * Compare.
     *
     * @param t1
     *            the t1
     * @param t2
     *            the t2
     * @return the int
     * @see org.apache.commons.lang3.ObjectUtils#compare(Comparable, Comparable)
     * @see org.apache.commons.lang3.ObjectUtils#compare(Comparable, Comparable, boolean)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int compare(T t1,T t2){
        if (t1 == t2){
            return 0;
        }else if (null == t1){//null 排在后面
            return 1;
        }else if (null == t2){
            return -1;
        }

        Comparable propertyValue1 = PropertyUtil.getProperty(t1, propertyName);
        Comparable propertyValue2 = PropertyUtil.getProperty(t2, propertyName);

        //如果值需要类型转换
        if (null != propertyValueConvertToClass){
            propertyValue1 = ConvertUtil.convert(propertyValue1, propertyValueConvertToClass);
            propertyValue2 = ConvertUtil.convert(propertyValue2, propertyValueConvertToClass);
        }
        return null != comparator ? comparator.compare(propertyValue1, propertyValue2) : compare(t1, t2, propertyValue1, propertyValue2);
    }

    /**
     * 先比较 propertyValue1以及propertyValue2,再比较 t1/t2 .
     * 
     * <p>
     * 由于我们是提取 property的特殊性, 如果只判断值的话, 那么 TreeSet / TreeMap 过滤掉同sort字段但是对象不相同的情况
     * </p>
     *
     * @param t1
     *            the t1
     * @param t2
     *            the t2
     * @param propertyValue1
     *            the property value1
     * @param propertyValue2
     *            the property value2
     * @return the int
     * @see org.apache.commons.collections4.comparators.ComparableComparator
     * @since 1.5.4
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int compare(T t1,T t2,Comparable propertyValue1,Comparable propertyValue2){
        //NullPointException if propertyValue1.compareTo(propertyValue2);
        int compareTo = ObjectUtils.compare(propertyValue1, propertyValue2);

        if (0 == compareTo){
            //避免TreeSet / TreeMap 过滤掉同sort字段但是对象不相同的情况
            int hashCode1 = t1.hashCode();
            int hashCode2 = t2.hashCode();
            compareTo = ObjectUtils.compare(hashCode1, hashCode2);

            String pattern = "propertyName:[{}],same propertyValue:[{}],hashCode1:[{}],hashCode2:[{}],result:[{}]";
            LOGGER.trace(pattern, propertyName, propertyValue1, hashCode1, hashCode2, compareTo);
            return compareTo;
        }

        String pattern = "propertyName:[{}],propertyValue1:[{}],propertyValue2:[{}],result:[{}]";
        LOGGER.trace(pattern, propertyName, propertyValue1, propertyValue2, compareTo);
        return compareTo;
    }
}
