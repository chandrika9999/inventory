import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class InventoryItem {
    private String id;
    private String name;
    private String category;
    private int quantity;

    public InventoryItem(String id, String name, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

class InventoryManagementSystem {
    private Map<String, InventoryItem> inventoryById = new HashMap<>();
    private Map<String, PriorityQueue<InventoryItem>> inventoryByCategory = new HashMap<>();
    private final int restockThreshold;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    private final Set<String> predefinedCategories = new HashSet<>(Arrays.asList(
            "Electronics", "Home Appliances", "Furniture", "Clothing", "Books", "Toys"
    ));

    public InventoryManagementSystem(int restockThreshold) {
        this.restockThreshold = restockThreshold;
    }

    public void addOrUpdateItem(String name, String category, int quantity) {
        if (!predefinedCategories.contains(category)) {
            System.out.println("Invalid category. Available categories are: " + predefinedCategories);
            return;
        }
        String id = generateUniqueId();
        InventoryItem item = new InventoryItem(id, name, category, quantity);
        inventoryById.put(id, item);

        inventoryByCategory.computeIfAbsent(category, k -> new PriorityQueue<>(Comparator.comparingInt(InventoryItem::getQuantity).reversed()))
                .add(item);

        checkRestocking(item);
        System.out.println("Item added successfully: " + item);
    }

    public void removeItemById(String id) {
        InventoryItem item = inventoryById.remove(id);
        if (item != null) {
            PriorityQueue<InventoryItem> categoryQueue = inventoryByCategory.get(item.getCategory());
            if (categoryQueue != null) {
                categoryQueue.remove(item);
            }
            System.out.println("Item removed successfully: " + item);
        } else {
            System.out.println("Item with ID " + id + " not found.");
        }
    }

    public InventoryItem getItemById(String id) {
        return inventoryById.get(id);
    }

    public List<InventoryItem> getItemsByCategory(String category) {
        PriorityQueue<InventoryItem> categoryQueue = inventoryByCategory.get(category);
        return categoryQueue != null ? new ArrayList<>(categoryQueue) : Collections.emptyList();
    }

    public void checkRestocking(InventoryItem item) {
        if (item.getQuantity() < restockThreshold) {
            System.out.println("Restock Notification: Item " + item.getName() + " (ID: " + item.getId() + ") needs restocking!");
        }
    }

    public void mergeInventories(InventoryManagementSystem other) {
        for (InventoryItem item : other.inventoryById.values()) {
            InventoryItem existingItem = inventoryById.get(item.getId());
            if (existingItem != null) {
                existingItem.setQuantity(Math.max(existingItem.getQuantity(), item.getQuantity()));
            } else {
                addOrUpdateItem(item.getName(), item.getCategory(), item.getQuantity());
            }
        }
    }

    public List<InventoryItem> getTopKItems(int k) {
        List<InventoryItem> allItems = new ArrayList<>(inventoryById.values());
        allItems.sort(Comparator.comparingInt(InventoryItem::getQuantity).reversed());
        return allItems.subList(0, Math.min(k, allItems.size()));
    }

    private String generateUniqueId() {
        return "ID" + idCounter.getAndIncrement();
    }

    public void displayInventory() {
        inventoryById.values().forEach(System.out::println);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InventoryManagementSystem ims = new InventoryManagementSystem(10);

        while (true) {
            System.out.println("Choose an operation: ");
            System.out.println("1. Add/Update Item");
            System.out.println("2. Remove Item");
            System.out.println("3. Display Item by ID");
            System.out.println("4. Get Items by Category");
            System.out.println("5. Merge Inventories");
            System.out.println("6. Get Top K Items");
            System.out.println("7. Display Inventory");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Item name: ");
                    String name = scanner.nextLine();
                    System.out.println("Categories: " + ims.predefinedCategories);
                    System.out.print("Category: ");
                    String category = scanner.nextLine();
                    System.out.print("Quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    ims.addOrUpdateItem(name, category, quantity);
                    break;
                case 2:
                    System.out.print("Enter item ID to remove: ");
                    String removeId = scanner.nextLine();
                    ims.removeItemById(removeId);
                    break;
                case 3:
                    System.out.print("Enter item ID to display: ");
                    String displayId = scanner.nextLine();
                    InventoryItem item = ims.getItemById(displayId);
                    if (item != null) {
                        System.out.println(item);
                    } else {
                        System.out.println("Item not found.");
                    }
                    break;
                case 4:
                    System.out.print("Enter category to retrieve items: ");
                    String cat = scanner.nextLine();
                    List<InventoryItem> itemsByCategory = ims.getItemsByCategory(cat);
                    itemsByCategory.forEach(System.out::println);
                    break;
                case 5:
                    System.out.println("Merge with another inventory.");
                    InventoryManagementSystem anotherIMS = new InventoryManagementSystem(10);
                    anotherIMS.addOrUpdateItem("New Item", "Electronics", 50);
                    ims.mergeInventories(anotherIMS);
                    break;
                case 6:
                    System.out.print("Enter k to get top k items: ");
                    int k = Integer.parseInt(scanner.nextLine());
                    List<InventoryItem> topItems = ims.getTopKItems(k);
                    topItems.forEach(System.out::println);
                    break;
                case 7:
                    ims.displayInventory();
                    break;
                case 8:
                    System.out.println("Exiting.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
