import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (context) => BudgetProvider(),
      child: BudgetApp(),
    ),
  );
}

class BudgetApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Personal Budget Manager',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: DashboardScreen(),
    );
  }
}

class BudgetProvider extends ChangeNotifier {
  double totalBudget = 5000;
  List<BudgetCategory> categories = [
    BudgetCategory(name: 'Food', allocated: 1000, spent: 800),
    BudgetCategory(name: 'Transport', allocated: 500, spent: 300),
    BudgetCategory(name: 'Entertainment', allocated: 1500, spent: 1000),
  ];

  void addCategory(String name, double allocated) {
    categories.add(BudgetCategory(name: name, allocated: allocated, spent: 0));
    notifyListeners();
  }

  void editCategory(int index, String name, double allocated) {
    categories[index].name = name;
    categories[index].allocated = allocated;
    notifyListeners();
  }

  void deleteCategory(int index) {
    categories.removeAt(index);
    notifyListeners();
  }

  void addTransaction(int index, String description, double amount) {
    categories[index].spent += amount;
    notifyListeners();
  }
}

class BudgetCategory {
  String name;
  double allocated;
  double spent;

  BudgetCategory({required this.name, required this.allocated, this.spent = 0});
}

class DashboardScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final budgetProvider = Provider.of<BudgetProvider>(context);
    
    return Scaffold(
      appBar: AppBar(title: Text('Personal Budget Manager')),
      body: Column(
        children: [
          Card(
            margin: EdgeInsets.all(10),
            child: Padding(
              padding: EdgeInsets.all(16),
              child: Text('Total Budget: \$${budgetProvider.totalBudget}',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: budgetProvider.categories.length,
              itemBuilder: (context, index) {
                final category = budgetProvider.categories[index];
                return ListTile(
                  title: Text(category.name),
                  subtitle: Text('Allocated: \$${category.allocated} | Spent: \$${category.spent}'),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: Icon(Icons.add),
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => AddTransactionScreen(index),
                            ),
                          );
                        },
                      ),
                      IconButton(
                        icon: Icon(Icons.edit),
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => AddCategoryScreen(editIndex: index),
                            ),
                          );
                        },
                      ),
                      IconButton(
                        icon: Icon(Icons.delete, color: Colors.red),
                        onPressed: () {
                          budgetProvider.deleteCategory(index);
                        },
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => AddCategoryScreen()));
            },
            child: Text('Add Category'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => InsightsScreen()));
            },
            child: Text('View Insights'),
          ),
        ],
      ),
    );
  }
}

class AddCategoryScreen extends StatefulWidget {
  final int? editIndex;
  AddCategoryScreen({this.editIndex});

  @override
  _AddCategoryScreenState createState() => _AddCategoryScreenState();
}

class _AddCategoryScreenState extends State<AddCategoryScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _amountController = TextEditingController();

  @override
  void initState() {
    super.initState();
    if (widget.editIndex != null) {
      final category = Provider.of<BudgetProvider>(context, listen: false).categories[widget.editIndex!];
      _nameController.text = category.name;
      _amountController.text = category.allocated.toString();
    }
  }

  @override
  Widget build(BuildContext context) {
    final budgetProvider = Provider.of<BudgetProvider>(context);

    return Scaffold(
      appBar: AppBar(title: Text(widget.editIndex == null ? 'Add Category' : 'Edit Category')),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(labelText: 'Category Name'),
                validator: (value) => value!.isEmpty ? 'Enter a category name' : null,
              ),
              TextFormField(
                controller: _amountController,
                decoration: InputDecoration(labelText: 'Allocated Amount'),
                keyboardType: TextInputType.number,
                validator: (value) {
                  if (value!.isEmpty || double.tryParse(value) == null) return 'Enter a valid amount';
                  if (double.parse(value) <= 0) return 'Amount must be greater than zero';
                  return null;
                },
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState!.validate()) {
                    if (widget.editIndex == null) {
                      budgetProvider.addCategory(_nameController.text, double.parse(_amountController.text));
                    } else {
                      budgetProvider.editCategory(widget.editIndex!, _nameController.text, double.parse(_amountController.text));
                    }
                    Navigator.pop(context);
                  }
                },
                child: Text('Save'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class AddTransactionScreen extends StatelessWidget {
  final int categoryIndex;
  AddTransactionScreen(this.categoryIndex);

  final _formKey = GlobalKey<FormState>();
  final _descController = TextEditingController();
  final _amountController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final budgetProvider = Provider.of<BudgetProvider>(context);
    final category = budgetProvider.categories[categoryIndex];

    return Scaffold(
      appBar: AppBar(title: Text('Add Transaction')),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _descController,
                decoration: InputDecoration(labelText: 'Description'),
                validator: (value) => value!.isEmpty ? 'Enter a description' : null,
              ),
              TextFormField(
                controller: _amountController,
                decoration: InputDecoration(labelText: 'Amount Spent'),
                keyboardType: TextInputType.number,
                validator: (value) {
                  if (value!.isEmpty || double.tryParse(value) == null) return 'Enter a valid amount';
                  if (double.parse(value) > category.allocated - category.spent) return 'Not enough budget';
                  return null;
                },
              ),
              SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState!.validate()) {
                    budgetProvider.addTransaction(categoryIndex, _descController.text, double.parse(_amountController.text));
                    Navigator.pop(context);
                  }
                },
                child: Text('Add Transaction'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
