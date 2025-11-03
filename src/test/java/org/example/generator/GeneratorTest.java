package org.example.generator;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Product;
import org.example.classes.Shape;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTest {

    private final Generator generator = new Generator(new Random(42), 3, 3);

    @Test
    void generatesExampleInstance() {
        assertDoesNotThrow(() -> {
            Object value = generator.generateValueOfType(org.example.classes.Example.class);
            assertNotNull(value);
            assertTrue(value instanceof org.example.classes.Example);
        });
    }

    @RepeatedTest(5)
    void generatesShapeImplementation() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Shape value = (Shape) generator.generateValueOfType(Shape.class);
        assertNotNull(value);
        System.out.println(value.getArea());
        assertTrue(value instanceof Shape);
    }

    @Test
    void generatesCartWithProducts() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Cart cart = (Cart) generator.generateValueOfType(Cart.class);
        assertNotNull(cart);
        List<Product> items = cart.getItems();
        assertNotNull(items);
        System.out.println(items.getFirst().getPrice());
        for (Product product : items) {
            assertNotNull(product);
        }
    }

    @Test
    void respectsConfiguredMaxDepthForBinaryTree() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        BinaryTreeNode root = (BinaryTreeNode) generator.generateValueOfType(BinaryTreeNode.class);
        assertNotNull(root);
        int depth = calculateDepth(root);
        System.out.println(depth);
        System.out.println(root.getData());
        assertTrue(depth <= 3);
    }

    private int calculateDepth(BinaryTreeNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(calculateDepth(node.getLeft()), calculateDepth(node.getRight()));
    }
}

